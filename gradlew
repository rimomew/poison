#!/bin/sh
set -eu
ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
GRADLE_VERSION=9.7.1
GRADLE_HOME="$ROOT_DIR/.gradle-dist/gradle-$GRADLE_VERSION"

if [ -x "$GRADLE_HOME/bin/gradle" ]; then
  exec "$GRADLE_HOME/bin/gradle" "$@"
fi

if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi

echo "Gradle $GRADLE_VERSION is not installed and this bootstrap expects curl/unzip." >&2
echo "On Windows, use gradlew.bat; it bootstraps the pinned distribution automatically." >&2
exit 1
