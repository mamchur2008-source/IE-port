param(
	[string]$MinecraftVersion = "26.1.2",
	[string]$ModVersion = "13.0.0",
	[string]$Repository = "user2047/ImmersiveEngineering",
	[string]$Target = "port/26.1.2-neoforge",
	[string]$ArtifactDir = "build/libs",
	[string]$NotesFile = "",
	[switch]$Draft,
	[switch]$Prerelease
)

$ErrorActionPreference = "Stop"

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $repoRoot

if(-not (Get-Command gh -ErrorAction SilentlyContinue))
{
	throw "GitHub CLI 'gh' was not found. Install it, reopen PowerShell, then run 'gh auth login'."
}

$artifactPrefix = "ImmersiveEngineering-$MinecraftVersion-$ModVersion"
$mainJar = Join-Path $ArtifactDir "$artifactPrefix.jar"
$apiJar = Join-Path $ArtifactDir "$artifactPrefix-api.jar"
$sourcesJar = Join-Path $ArtifactDir "$artifactPrefix-sources.jar"

if(-not (Test-Path -LiteralPath $mainJar))
{
	throw "Expected release jar not found: $mainJar"
}

$assets = @((Resolve-Path -LiteralPath $mainJar).Path)
foreach($optionalAsset in @($apiJar, $sourcesJar))
{
	if(Test-Path -LiteralPath $optionalAsset)
	{
		$assets += (Resolve-Path -LiteralPath $optionalAsset).Path
	}
}

if($NotesFile -eq "")
{
	$defaultNotes = @(
		(Join-Path ".dist" "$artifactPrefix/RELEASE_NOTES.md"),
		(Join-Path "..\.dist" "$artifactPrefix/RELEASE_NOTES.md")
	)
	foreach($candidateNotes in $defaultNotes)
	{
		if(Test-Path -LiteralPath $candidateNotes)
		{
			$NotesFile = $candidateNotes
			break
		}
	}
}

$tag = $ModVersion
$title = "ImmersiveEngineering $MinecraftVersion-$ModVersion"
$ghArgs = @("release", "create", $tag)
$ghArgs += $assets
$ghArgs += @(
	"--repo", $Repository,
	"--target", $Target,
	"--title", $title
)

if($NotesFile -ne "")
{
	$ghArgs += @("--notes-file", (Resolve-Path -LiteralPath $NotesFile).Path)
}
else
{
	$ghArgs += @("--notes", "Minecraft $MinecraftVersion release for Immersive Engineering $ModVersion.")
}

if($Draft)
{
	$ghArgs += "--draft"
}

if($Prerelease)
{
	$ghArgs += "--prerelease"
}

Write-Host "Creating GitHub release '$tag' in $Repository"
Write-Host "Target: $Target"
Write-Host "Assets:"
$assets | ForEach-Object { Write-Host " - $_" }

& gh @ghArgs
