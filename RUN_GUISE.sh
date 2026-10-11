#!/usr/bin/env sh
cd "$(dirname "$0")" || exit 1
exec java -jar dist/GUISE-Band-A.jar
