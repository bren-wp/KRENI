#!/usr/bin/env python3
"""Build the KRENI app identity as a real PNG asset using Python stdlib.

Output is a deterministic 1024px icon for Xcode, generated from the same
route-K motif as Android VectorDrawable and assets/brand/mark.svg.
"""
from pathlib import Path
import math
import struct
import zlib

SIZE = 1024
SCALE = SIZE / 512
ROUTES = [
    (146, 105, 146, 348),
    (146, 348, 105, 419),
    (146, 282, 326, 124),
    (164, 272, 362, 407),
]
NODES = [(146, 105), (146, 348), (238, 210)]


def distance_squared(x, y, segment):
    ax, ay, bx, by = segment
    dx, dy = bx - ax, by - ay
    length_squared = dx * dx + dy * dy
    t = max(0., min(1., ((x - ax) * dx + (y - ay) * dy) / length_squared))
    return (x - ax - t * dx) ** 2 + (y - ay - t * dy) ** 2


def chunk(name, body):
    return struct.pack(">I", len(body)) + name + body + struct.pack(
        ">I", zlib.crc32(name + body) & 0xffffffff)


def main():
    rows = bytearray()
    for j in range(SIZE):
        y = (j + 0.5) / SCALE
        rows.append(0)
        for i in range(SIZE):
            x = (i + 0.5) / SCALE
            r, g, b = 7, 25, 54
            if any(distance_squared(x, y, seg) <= 33 ** 2 for seg in ROUTES):
                t = x / 512
                r = round(20 + (2 - 20) * t)
                g = round(107 + (205 - 107) * t)
                b = round(252 + (189 - 252) * t)
            for nx, ny in NODES:
                d = (x - nx) ** 2 + (y - ny) ** 2
                if d <= 38 ** 2:
                    r, g, b = 255, 255, 255
                if d <= 17 ** 2:
                    r, g, b = 9, 90, 183
            rows.extend((r, g, b, 255))
    binary = (
        b"\x89PNG\r\n\x1a\n"
        + chunk(b"IHDR", struct.pack(">IIBBBBB", SIZE, SIZE, 8, 6, 0, 0, 0))
        + chunk(b"IDAT", zlib.compress(rows, level=6))
        + chunk(b"IEND", b"")
    )
    output = Path("ios/KRENI/Assets.xcassets/AppIcon.appiconset/AppIcon.png")
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_bytes(binary)
    assert output.stat().st_size > 10000
    print(f"Created {output} ({output.stat().st_size} bytes)")


if __name__ == "__main__":
    main()
