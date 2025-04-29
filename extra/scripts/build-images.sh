#!/bin/bash
defaultVersion="0.0.1"
echo "********* building ********"
read -p "enter build version default ${defaultVersion}: " version

if [ -z "$version" ]; then
    version=$defaultVersion
fi
echo "$version"
./gradlew clean bootBuildImage -x test -Dorg.gradle.project.version="${version}" --stacktrace