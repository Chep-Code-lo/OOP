#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

bash build.sh

JAVA_CMD=${JAVA:-}
if [[ -z "$JAVA_CMD" ]]; then
    if command -v java >/dev/null 2>&1; then
        JAVA_CMD="java"
    elif [[ -x "/mnt/c/Program Files/Java/jdk-21/bin/java.exe" ]]; then
        JAVA_CMD="/mnt/c/Program Files/Java/jdk-21/bin/java.exe"
    else
        echo "Error: java not found. Set JAVA=/path/to/java and rerun." >&2
        exit 1
    fi
fi

printf 'Running functional tests...\n'
"$JAVA_CMD" -cp out app.TestRunner

printf 'Running scripted UI demo...\n'
"$JAVA_CMD" -cp out app.TestScript

printf 'Running CLI smoke test...\n'
"$JAVA_CMD" -cp out app.App <<<'0'
printf '\nAll tests completed successfully.\n'
