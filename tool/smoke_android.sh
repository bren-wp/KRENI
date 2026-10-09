#!/usr/bin/env bash
set -euo pipefail
APK="$(find dist/android -maxdepth 1 -type f -name 'KRENI-Android-v*-installable-test.apk' -print -quit)"
test -n "$APK"
echo "Installing package: $APK"
test -s "$APK"
adb install -r "$APK"
mkdir -p dist/screenshots/android
for item in home:0 map:1 departures:2 more:3; do
  screen="${item%%:*}"
  index="${item##*:}"
  adb shell am force-stop hr.brendigo.kreni || true
  adb shell am start -W -n hr.brendigo.kreni/.MainActivity --ei kreni_capture_tab "$index"
  sleep 5
  adb shell pidof hr.brendigo.kreni >/dev/null
  adb exec-out screencap -p > "dist/screenshots/android/$screen.png"
  python3 - "dist/screenshots/android/$screen.png" <<'PY'
from pathlib import Path
import struct, sys
p = Path(sys.argv[1])
raw = p.read_bytes()
assert raw.startswith(b"\x89PNG\r\n\x1a\n")
assert len(raw) > 10000
width, height = struct.unpack(">II", raw[16:24])
assert width >= 300 and height >= 600
print(f"{p}: {width}x{height} ({len(raw)} bytes)")
PY
done
