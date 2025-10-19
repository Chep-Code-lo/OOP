#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

OUT_DIR="out"
SOURCES_TXT="${OUT_DIR}/sources.txt"

# Allow override via JAVAC env var, otherwise fall back to system javac or common Windows install.
if [[ -n "${JAVAC:-}" ]]; then
    JAVAC_CMD=("$JAVAC")
elif command -v javac >/dev/null 2>&1; then
    JAVAC_CMD=("javac")
elif [[ -x "/mnt/c/Program Files/Java/jdk-21/bin/javac.exe" ]]; then
    JAVAC_CMD=("/mnt/c/Program Files/Java/jdk-21/bin/javac.exe")
else
    echo "Error: javac not found. Set JAVAC=/path/to/javac and rerun." >&2
    exit 1
fi

rm -rf "$OUT_DIR"
mkdir -p "$OUT_DIR"

if [[ "${JAVAC_CMD[0]}" == *.exe ]]; then
    find src/main/java -name '*.java' -print0 \
        | xargs -0 -I{} wslpath -w "{}" \
        | sort > "$SOURCES_TXT"
else
    find src/main/java -name '*.java' | sort > "$SOURCES_TXT"
fi

"${JAVAC_CMD[@]}" -d "$OUT_DIR" @"$SOURCES_TXT"

FILE_COUNT=$(wc -l < "$SOURCES_TXT" | tr -d '[:space:]')
echo "Compiled $FILE_COUNT source files into $OUT_DIR/"
