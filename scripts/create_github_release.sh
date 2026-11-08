#!/usr/bin/env bash
set -euo pipefail

minecraft_version="26.1.2"
mod_version="13.0.0"
repository="user2047/ImmersiveEngineering"
target="port/26.1.2-neoforge"
artifact_dir="build/libs"
notes_file=""
draft=false
prerelease=false

usage() {
	cat <<'EOF'
Usage: ./scripts/create_github_release.sh [options]

Creates a GitHub release using GitHub CLI.

Defaults:
  Jar name: ImmersiveEngineering-26.1.2-13.0.0.jar
  Tag:      13.0.0
  Repo:     user2047/ImmersiveEngineering
  Target:   port/26.1.2-neoforge

Options:
  --minecraft-version VALUE   Minecraft version, default 26.1.2
  --mod-version VALUE         Mod/release version, default 13.0.0
  --repo VALUE                GitHub repo, default user2047/ImmersiveEngineering
  --target VALUE              Release target branch or commit, default port/26.1.2-neoforge
  --artifact-dir VALUE        Folder containing jars, default build/libs
  --notes-file VALUE          Release notes markdown file
  --draft                     Create a draft release
  --prerelease                Mark as prerelease
  -h, --help                  Show this help
EOF
}

while [[ $# -gt 0 ]]; do
	case "$1" in
		--minecraft-version)
			minecraft_version="$2"
			shift 2
			;;
		--mod-version)
			mod_version="$2"
			shift 2
			;;
		--repo)
			repository="$2"
			shift 2
			;;
		--target)
			target="$2"
			shift 2
			;;
		--artifact-dir)
			artifact_dir="$2"
			shift 2
			;;
		--notes-file)
			notes_file="$2"
			shift 2
			;;
		--draft)
			draft=true
			shift
			;;
		--prerelease)
			prerelease=true
			shift
			;;
		-h|--help)
			usage
			exit 0
			;;
		*)
			echo "Unknown option: $1" >&2
			usage >&2
			exit 2
			;;
	esac
done

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
repo_root="$(cd -- "$script_dir/.." && pwd)"
cd "$repo_root"

if ! command -v gh >/dev/null 2>&1; then
	echo "GitHub CLI 'gh' was not found." >&2
	echo "Install it, restart Git Bash, then run: gh auth login" >&2
	exit 1
fi

artifact_prefix="ImmersiveEngineering-${minecraft_version}-${mod_version}"
main_jar="${artifact_dir}/${artifact_prefix}.jar"
api_jar="${artifact_dir}/${artifact_prefix}-api.jar"
sources_jar="${artifact_dir}/${artifact_prefix}-sources.jar"

if [[ ! -f "$main_jar" ]]; then
	echo "Expected release jar not found: $main_jar" >&2
	exit 1
fi

assets=("$main_jar")
for optional_asset in "$api_jar" "$sources_jar"; do
	if [[ -f "$optional_asset" ]]; then
		assets+=("$optional_asset")
	fi
done

if [[ -z "$notes_file" ]]; then
	for candidate in \
		".dist/${artifact_prefix}/RELEASE_NOTES.md" \
		"../.dist/${artifact_prefix}/RELEASE_NOTES.md"; do
		if [[ -f "$candidate" ]]; then
			notes_file="$candidate"
			break
		fi
	done
fi

tag="$mod_version"
title="ImmersiveEngineering ${minecraft_version}-${mod_version}"
gh_args=(release create "$tag")
gh_args+=("${assets[@]}")
gh_args+=(--repo "$repository" --target "$target" --title "$title")

if [[ -n "$notes_file" ]]; then
	gh_args+=(--notes-file "$notes_file")
else
	gh_args+=(--notes "Minecraft ${minecraft_version} release for Immersive Engineering ${mod_version}.")
fi

if [[ "$draft" == true ]]; then
	gh_args+=(--draft)
fi

if [[ "$prerelease" == true ]]; then
	gh_args+=(--prerelease)
fi

echo "Creating GitHub release '$tag' in $repository"
echo "Target: $target"
echo "Assets:"
printf ' - %s\n' "${assets[@]}"

gh "${gh_args[@]}"
