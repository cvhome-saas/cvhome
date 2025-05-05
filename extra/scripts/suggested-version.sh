#!/bin/bash

# Exit immediately if a command exits with a non-zero status.
set -e
# Treat unset variables as an error when substituting.
set -u
# Pipelines return status of the last command to exit with non-zero status,
# or zero if all commands exit successfully.
set -o pipefail

# --- Configuration ---
GRADLE_PROPERTIES_FILE="gradle.properties"
VERSION_PROPERTY_NAME="version"

# --- Input Validation ---
if [ -z "${1:-}" ]; then # Check if $1 is set and not empty
    echo "Error: Build number argument is required." >&2
    exit 1
fi
build_number="$1"

if [ ! -f "$GRADLE_PROPERTIES_FILE" ]; then
    echo "Error: Gradle properties file not found: '$GRADLE_PROPERTIES_FILE'" >&2
    exit 1
fi

# --- Read Version ---
# Grep for the version line, excluding commented lines and handling potential spaces around '='
version_line_str=$(grep -E "^[[:space:]]*${VERSION_PROPERTY_NAME}[[:space:]]*=" "$GRADLE_PROPERTIES_FILE")

if [ -z "$version_line_str" ]; then
    echo "Error: Could not find '$VERSION_PROPERTY_NAME' property in '$GRADLE_PROPERTIES_FILE'." >&2
    exit 1
fi

# Extract the version value using sed for more flexibility
# Removes prefix 'version=' and trims leading/trailing whitespace (though grep above helps)
gradle_version=$(echo "$version_line_str" | sed -E "s/^[[:space:]]*${VERSION_PROPERTY_NAME}[[:space:]]*=//; s/^[[:space:]]+|[[:space:]]+$//")

# --- Get and Sanitize Branch Name ---
current_branch=$(git rev-parse --abbrev-ref HEAD)
# Replace all slashes '/' with hyphens '-' for Docker tag compatibility
sanitized_branch="${current_branch//\//-}"

# --- Determine Tag Logic ---
# Check if the version string ends with -SNAPSHOT
if [[ "$gradle_version" == *-SNAPSHOT ]]; then
    # It's a SNAPSHOT version
    # Extract base version (part before the last '-')
    base_version="${gradle_version%-*}"
    # Construct the tag: sanitized_branch-base_version-build_number-SNAPSHOT
    echo "${sanitized_branch}-${base_version}-${build_number}-SNAPSHOT"
else
    # It's a release version, use it directly
    echo "$gradle_version"
fi

exit 0 # Explicitly exit successfully