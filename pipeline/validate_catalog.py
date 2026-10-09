import json
from pathlib import Path


def validate(path: Path) -> None:
    catalog = json.loads(path.read_text(encoding="utf-8"))
    assert catalog["schemaVersion"] == 1
    cities = {x["id"]: x for x in catalog["cities"]}
    assert len(cities) == len(catalog["cities"])
    stops = {x["id"]: x for x in catalog["stops"]}
    assert len(stops) == len(catalog["stops"])
    for stop in stops.values():
        assert stop["city"] in cities
        assert stop["name"].strip()
        assert -90 <= stop["lat"] <= 90 and -180 <= stop["lon"] <= 180
        assert set(stop["modes"]) <= {"bus", "tram", "train", "ferry", "catamaran"}
        if "tram" in stop["modes"]:
            assert cities[stop["city"]]["trams"]
    print(f"Catalog: {len(cities)} cities, {len(stops)} locations validated")


if __name__ == "__main__":
    validate(Path("data/catalog.json"))
