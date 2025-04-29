#!/bin/bash

build_number=$1

version_line_str=$(grep version gradle.properties)
current_version="${version_line_str#version=}"
current_branch=$(git rev-parse --abbrev-ref HEAD)
if grep -q "\\-SNAPSHOT" <<<"$current_version"; then
    base_version="${current_version%-*}"
    echo "$current_branch-$base_version-${build_number}-SNAPSHOT"
else
    echo "$current_version"
fi