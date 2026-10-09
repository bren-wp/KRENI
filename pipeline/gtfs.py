"""Database-free, strict GTFS ZIP validator and deterministic scheduled-routing graph.

Reads existing approved GTFS archives only. No scraping, tokens, SQL or GPS guesses.
Does not publish data unless explicitly invoked with a license-reviewed input.
"""
from __future__ import annotations

import argparse
import csv
import datetime as dt
import hashlib
import io
import json
import re
import zipfile
from collections import defaultdict
from pathlib import Path
from zoneinfo import ZoneInfo

TIME = re.compile(r"^\d{1,3}:[0-5]\d:[0-5]\d$")
DAY = re.compile(r"^\d{8}$")
REQUIRED = {
    "agency.txt": {"agency_name", "agency_url", "agency_timezone"},
    "stops.txt": {"stop_id", "stop_name", "stop_lat", "stop_lon"},
    "routes.txt": {"route_id", "route_type"},
    "trips.txt": {"route_id", "service_id", "trip_id"},
    "stop_times.txt": {"trip_id", "arrival_time", "departure_time", "stop_id", "stop_sequence"},
}


def seconds(text: str) -> int:
    if not TIME.fullmatch(text):
        raise ValueError(f"Invalid GTFS time: {text!r}")
    h, m, s = map(int, text.split(":"))
    if h > 47:
        raise ValueError("GTFS time outside the supported 48h service-day window")
    return h * 3600 + m * 60 + s


def parse_day(text: str) -> dt.date:
    if not DAY.fullmatch(text):
        raise ValueError("Invalid GTFS date")
    return dt.datetime.strptime(text, "%Y%m%d").date()


def rows(bundle: zipfile.ZipFile, name: str, required: set[str]) -> list[dict[str, str]]:
    if name not in bundle.namelist():
        raise ValueError(f"Missing required file: {name}")
    info = bundle.getinfo(name)
    if info.file_size > 100 * 1024 * 1024:
        raise ValueError(f"Oversized table: {name}")
    raw = bundle.read(name).decode("utf-8-sig", errors="strict")
    csv_reader = csv.DictReader(io.StringIO(raw, newline=""))
    if not csv_reader.fieldnames or not required.issubset(csv_reader.fieldnames):
        raise ValueError(f"Missing required columns in {name}")
    result = list(csv_reader)
    if len(result) > 3_000_000:
        raise ValueError(f"Too many rows in {name}")
    if any(None in entry for entry in result):
        raise ValueError(f"Corrupted CSV row in {name}")
    return result


def active_services(bundle: zipfile.ZipFile, target: dt.date) -> set[str]:
    day = target.strftime("%Y%m%d")
    enabled: set[str] = set()
    if "calendar.txt" in bundle.namelist():
        data = rows(bundle, "calendar.txt", {
            "service_id", "start_date", "end_date", "monday", "tuesday",
            "wednesday", "thursday", "friday", "saturday", "sunday"
        })
        weekday = target.strftime("%A").lower()
        enabled = {
            entry["service_id"] for entry in data
            if parse_day(entry["start_date"]) <= target <= parse_day(entry["end_date"])
            and entry[weekday] == "1"
        }
    if "calendar_dates.txt" in bundle.namelist():
        exceptions = rows(bundle, "calendar_dates.txt", {
            "service_id", "date", "exception_type"
        })
        for entry in exceptions:
            if entry["date"] == day:
                if entry["exception_type"] == "1":
                    enabled.add(entry["service_id"])
                elif entry["exception_type"] == "2":
                    enabled.discard(entry["service_id"])
                else:
                    raise ValueError("Invalid calendar exception type")
    if "calendar.txt" not in bundle.namelist() and "calendar_dates.txt" not in bundle.namelist():
        raise ValueError("GTFS feed contains no service calendar")
    return enabled


def load_gtfs(path: Path, provider: str, target: dt.date) -> dict:
    if not provider or not re.fullmatch(r"[a-z0-9_-]{2,40}", provider):
        raise ValueError("Provider ID must be a unique short slug")
    if path.stat().st_size > 150 * 1024 * 1024:
        raise ValueError("GTFS archive too large")
    digest = hashlib.sha256(path.read_bytes()).hexdigest()
    with zipfile.ZipFile(path) as bundle:
        all_files = bundle.namelist()
        if len(all_files) > 40 or any(
            name.startswith("/") or ".." in Path(name).parts for name in all_files
        ):
            raise ValueError("Unsafe ZIP structure")
        if sum(info.file_size for info in bundle.infolist()) > 300 * 1024 * 1024:
            raise ValueError("Excessive uncompressed ZIP size")
        records = {name: rows(bundle, name, columns) for name, columns in REQUIRED.items()}
        agency = records["agency.txt"]
        zones = {record["agency_timezone"] for record in agency}
        if len(zones) != 1 or zones != {"Europe/Zagreb"}:
            raise ValueError("The Croatia feed must use Europe/Zagreb timezone")
        ZoneInfo("Europe/Zagreb")
        services = active_services(bundle, target)
        stops = {}
        for record in records["stops.txt"]:
            name = record["stop_name"].strip()
            lat, lon = float(record["stop_lat"]), float(record["stop_lon"])
            if not (-90 <= lat <= 90 and -180 <= lon <= 180) or not name:
                raise ValueError("Invalid coordinates or stop name")
            stop_id = record["stop_id"].strip()
            if not stop_id or stop_id in stops:
                raise ValueError("Missing/duplicate stop_id")
            stops[stop_id] = {"id": f"{provider}:{stop_id}", "name": name,
                              "lat": lat, "lon": lon}
        routes = {}
        for record in records["routes.txt"]:
            route_id = record["route_id"]
            if not route_id or route_id in routes:
                raise ValueError("Missing/duplicate route_id")
            routes[route_id] = record
        trips = {}
        for record in records["trips.txt"]:
            trip_id = record["trip_id"]
            if not trip_id or trip_id in trips or record["route_id"] not in routes:
                raise ValueError("Invalid route/trip reference or duplicate trip_id")
            trips[trip_id] = record
        services_in_file = {trip["service_id"] for trip in trips.values()}
        if "calendar.txt" in all_files:
            cal = rows(bundle, "calendar.txt", {"service_id", "start_date", "end_date"})
            known_services = {entry["service_id"] for entry in cal}
        else:
            known_services = set()
        if "calendar_dates.txt" in all_files:
            known_services.update(entry["service_id"] for entry in rows(
                bundle, "calendar_dates.txt", {"service_id", "date", "exception_type"}
            ))
        if not services_in_file.issubset(known_services):
            raise ValueError("Unresolved service_id reference")

        timelines: dict[str, list[tuple[int, int, int, str]]] = defaultdict(list)
        for record in records["stop_times.txt"]:
            tid, sid = record["trip_id"], record["stop_id"]
            if tid not in trips or sid not in stops:
                raise ValueError("Unresolved trip/stop reference")
            arr, dep = seconds(record["arrival_time"]), seconds(record["departure_time"])
            index = int(record["stop_sequence"])
            if index < 0 or arr > dep:
                raise ValueError("Invalid time or sequence")
            if trips[tid]["service_id"] in services:
                timelines[tid].append((index, arr, dep, sid))
        edges = []
        for tid, timetable in timelines.items():
            ordered = sorted(timetable)
            if len({record[0] for record in ordered}) != len(ordered):
                raise ValueError("Duplicated stop_sequence")
            for a, b in zip(ordered, ordered[1:]):
                if b[1] < a[2]:
                    raise ValueError("Arrival before departure on next stop")
                if a[3] == b[3]:
                    continue
                route = routes[trips[tid]["route_id"]]
                edges.append({
                    "from": f"{provider}:{a[3]}",
                    "to": f"{provider}:{b[3]}",
                    "depart": a[2], "arrive": b[1],
                    "trip": f"{provider}:{tid}",
                    "route": f"{provider}:{trips[tid]['route_id']}",
                    "routeType": int(route["route_type"]),
                    "kind": "scheduled",
                })
        if len(edges) > 2_000_000:
            raise ValueError("Graph exceeds size limit")
        return {
            "schemaVersion": 1,
            "source": provider,
            "serviceDate": target.isoformat(),
            "timezone": "Europe/Zagreb",
            "sha256": digest,
            "generatedAt": dt.datetime.now(dt.timezone.utc).isoformat(),
            "hasRealtime": False,
            "stops": list(stops.values()),
            "edges": edges,
        }


def earliest_route(graph: dict, origin: str, destination: str, earliest: int,
                   max_transfers: int = 4) -> list[dict]:
    """Time-respecting journey across published scheduled trip edges.

    Transfers happen only at exactly matching GTFS stop IDs; no guessed walking
    links between nearby coordinates. A 5-minute margin applies between trips.
    """
    edges = sorted(graph["edges"], key=lambda e: (e["depart"], e["arrive"]))
    best: dict[tuple[str, str | None, int], tuple[int, list[dict]]] = {
        (origin, None, 0): (earliest, [])
    }
    for edge in edges:
        if edge["arrive"] < edge["depart"]:
            continue
        candidates = list(best.items())
        for (stop, last_trip, transfers), (ready, itinerary) in candidates:
            if stop != edge["from"]:
                continue
            change = 0 if last_trip is None or last_trip == edge["trip"] else 1
            if transfers + change > max_transfers:
                continue
            margin = 300 if change else 0
            if ready + margin > edge["depart"]:
                continue
            key = (edge["to"], edge["trip"], transfers + change)
            arrival = edge["arrive"]
            previous = best.get(key)
            if previous is None or arrival < previous[0]:
                best[key] = (arrival, itinerary + [edge])
    options = [(data[0], data[1]) for key, data in best.items()
               if key[0] == destination and data[1]]
    return min(options, default=(None, []), key=lambda x: x[0])[1]


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("archive", type=Path)
    parser.add_argument("--provider", required=True)
    parser.add_argument("--date", type=dt.date.fromisoformat, required=True)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    graph = load_gtfs(args.archive, args.provider, args.date)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    tmp = args.output.with_suffix(".tmp")
    tmp.write_text(json.dumps(graph, ensure_ascii=False, separators=(",", ":")), encoding="utf-8")
    tmp.replace(args.output)
    print(f"Validated {len(graph['stops'])} stops; {len(graph['edges'])} scheduled edges.")


if __name__ == "__main__":
    main()
