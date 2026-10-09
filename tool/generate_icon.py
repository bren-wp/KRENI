#!/usr/bin/env python3
"""Generate deterministic KRENI launcher PNG using only Python stdlib.

Invoked by GitHub Actions before flutter_launcher_icons and flutter_native_splash.
The icon is derived from the route-K geometry in assets/brand/mark.svg.
"""
from __future__ import annotations

import struct
import zlib
from pathlib import Path

SIZE = 1024
SCALE = SIZE / 512
SEGMENTS = (
    (146, 105, 146, 348),
    (146, 348, 105, 419),
    (146, 282, 326, 124),
    (164, 272, 362, 407),
)
NODES = ((146, 105), (146, 348), (238, 210))


def distance_squared(px: float, py: float, a: tuple[int, int, int, int]) -> float:
    ax, ay, bx, by = a
    dx, dy = bx - ax, by - ay
    length_sq = dx * dx + dy * dy
    t = max(0.0, min(1.0, ((px - ax) * dx + (py - ay) * dy) / length_sq))
    ox, oy = px - (ax + t * dx), py - (ay + t * dy)
    return ox * ox + oy * oy


def png_chunk(name: bytes, value: bytes) -> bytes:
    payload = name + value
    return struct.pack(">I", len(value)) + payload + struct.pack(
        ">I", zlib.crc32(payload) & 0xFFFFFFFF
    )


def generate(path: Path) -> None:
    pixels = bytearray()
    radius_sq = 33.5**2
    white_sq = 39**2
    core_sq = 17**2
    for yy in range(SIZE):
        py = (yy + 0.5) / SCALE
        pixels.append(0)  # PNG scanline filter: none
        for xx in range(SIZE):
            px = (xx + 0.5) / SCALE
            mix = 0.5 * (px + py) / 512
            color = (
                round(8 + mix * 6),
                round(28 + mix * 17),
                round(61 + mix * 28),
                255,
            )
            if any(distance_squared(px, py, line) <= radius_sq for line in SEGMENTS):
                t = px / 512
                color = (
                    round(16 * (1 - t) + 1 * t),
                    round(107 * (1 - t) + 218 * t),
                    round(252 * (1 - t) + 178 * t),
                    255,
                )
            for cx, cy in NODES:
                d = (px - cx)**2 + (py - cy)**2
                if d <= white_sq:
                    color = (255, 255, 255, 255)
                if d <= core_sq:
                    color = (9, 90, 183, 255)
            pixels.extend(color)
    raw = (
        b"\x89PNG\r\n\x1a\n"
        + png_chunk(b"IHDR", struct.pack(">2I5B", SIZE, SIZE, 8, 6, 0, 0, 0))
        + png_chunk(b"IDAT", zlib.compress(pixels, level=6))
        + png_chunk(b"IEND", b"")
    )
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(raw)
    if path.stat().st_size < 5000:
        raise RuntimeError("Generated icon is unexpectedly small.")
    print(f"Created {path}: {path.stat().st_size} bytes")


if __name__ == "__main__":
    generate(Path("assets/brand/launcher.png"))
