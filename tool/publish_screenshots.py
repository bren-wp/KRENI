#!/usr/bin/env python3
"""Include screenshots in README only after CI captures eight actual app screens."""
from pathlib import Path
import struct

screens = ("home", "map", "departures", "more")
for platform in ("android", "ios"):
    for screen in screens:
        path = Path("docs/screenshots") / platform / (screen + ".png")
        data = path.read_bytes()
        if not data.startswith(b"\x89PNG\r\n\x1a\n") or len(data) < 10000:
            raise SystemExit("Invalid real-device screenshot: " + str(path))
        width, height = struct.unpack(">II", data[16:24])
        if width < 300 or height < 600:
            raise SystemExit("Unexpected screenshot geometry: " + str(path))

file = Path("README.md")
readme = file.read_text(encoding="utf-8")
marker = "<!-- KRENI_REAL_SCREENSHOTS -->"
if marker not in readme:
    start = readme.find("### Stvarni zasloni aplikacije")
    end = readme.find("## Preuzmi aplikaciju", start)
    if start < 0 or end < 0:
        raise SystemExit("Expected README screenshot section not found")
    readme = readme[:start] + marker + "\n\n" + readme[end:]
lines = [
    "### Stvarni zasloni aplikacije", "",
    "Snimke su nastale instalacijom i pokretanjem aplikacija u Android emulatoru i iOS simulatoru; nisu ilustracije.", ""
]
for platform in ("android", "ios"):
    lines += ["**" + platform.upper() + "**", "", '<div align="center">']
    for screen, label in zip(screens, ("Početna", "Karta", "Polasci", "Postavke")):
        lines.append(
            '<img src="docs/screenshots/' + platform + '/' + screen
            + '.png" alt="KRENI ' + platform + ' — ' + label
            + ', stvarna snimka zaslona" width="185" />'
        )
    lines += ["</div>", ""]
file.write_text(readme.replace(marker, "\n".join(lines)), encoding="utf-8")
print("Validated eight emulator/simulator captures and updated README.")
