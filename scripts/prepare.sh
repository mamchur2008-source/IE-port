#!/usr/bin/env bash
# Builds the working tree "ie/" : upstream IE 1.21.1 + vendored libs + overlay + mechanical migration + port.patch
set -euo pipefail
HERE="$(cd "$(dirname "$0")/.." && pwd)"
cd "$HERE"
IE_SHA=75a27f03e4243544243567e8d5c38d336f4f10f4
BMS_SHA=18204185b3f8487367258c2ce0a525a808614656
DC_SHA=853e34e

rm -rf ie _bms _dc
git clone -q https://github.com/BluSunrize/ImmersiveEngineering ie
git -C ie checkout -q "$IE_SHA"
rm -rf ie/.git ie/.github ie/.idea ie/src/test ie/src/gametest ie/scripts ie/Jenkinsfile

# vendored libraries (their maven releases don't exist for 26.1)
git clone -q https://github.com/malte0811/BlockModelSplitter _bms
git -C _bms checkout -q "$BMS_SHA"
git clone -q https://github.com/malte0811/DualCodecs _dc
git -C _dc checkout -q "$DC_SHA"
mkdir -p ie/src/lib/java
cp -r _bms/src/main/java/. ie/src/lib/java/
cp -r _dc/src/main/java/. ie/src/lib/java/
cp -r _dc/src/generated/java/. ie/src/lib/java/
rm -rf _bms _dc

# build files
rm -f ie/build.gradle ie/settings.gradle ie/gradle.properties
rm -rf ie/gradle ie/gradlew ie/gradlew.bat
cp -r overlay/. ie/

# third-party mod integrations are off for the first pass (JEI, CC:Tweaked, TOP, Jade, ...)
C=ie/src/main/java/blusunrize/immersiveengineering/common/util/compat
rm -rf $C/jei $C/computers/cctweaked $C/top $C/jade ie/src/main/oldjava

# mechanical migration driven by the NeoForge primers
python3 -I scripts/migrate.py ie/src

# baseline commit, so that `git diff` against it gives port.patch
cd ie
git init -q
git add -A
git -c user.name=port -c user.email=port@example.invalid commit -qm baseline
if [ -s ../port.patch ]; then git apply --whitespace=nowarn ../port.patch; fi
