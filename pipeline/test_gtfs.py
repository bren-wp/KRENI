import datetime as dt
import io
import tempfile
import unittest
import zipfile
from pathlib import Path

from pipeline.gtfs import seconds, load_gtfs, earliest_route

FILES = {
    "agency.txt": "agency_name,agency_url,agency_timezone\nTest,https://example.org,Europe/Zagreb\n",
    "stops.txt": "stop_id,stop_name,stop_lat,stop_lon\nA,A,45.8,15.9\nB,B,45.7,16.0\nC,C,45.6,16.1\n",
    "routes.txt": "route_id,route_type\nR1,2\nR2,3\n",
    "trips.txt": "route_id,service_id,trip_id\nR1,WORK,T1\nR2,WORK,T2\n",
    "stop_times.txt": "trip_id,arrival_time,departure_time,stop_id,stop_sequence\nT1,08:00:00,08:00:00,A,1\nT1,08:30:00,08:30:00,B,2\nT2,08:40:00,08:40:00,B,1\nT2,09:10:00,09:10:00,C,2\n",
    "calendar.txt": "service_id,monday,tuesday,wednesday,thursday,friday,saturday,sunday,start_date,end_date\nWORK,1,1,1,1,1,0,0,20260101,20261231\n",
}


def archive(path: Path, changes=None):
    content = FILES | (changes or {})
    with zipfile.ZipFile(path, "w") as z:
        for name, data in content.items():
            z.writestr(name, data)


class GTFSChecks(unittest.TestCase):
    def test_service_and_transfer(self):
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / "test.zip"
            archive(path)
            graph = load_gtfs(path, "test", dt.date(2026, 10, 9))
            self.assertEqual(len(graph["edges"]), 2)
            result = earliest_route(graph, "test:A", "test:C", seconds("07:59:00"))
            self.assertEqual([x["trip"] for x in result], ["test:T1", "test:T2"])
            self.assertEqual(earliest_route(graph, "test:A", "test:C", seconds("09:00:00")), [])
            self.assertEqual(load_gtfs(path, "test", dt.date(2026, 10, 10))["edges"], [])

    def test_missing_stop_reference_rejected(self):
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / "test.zip"
            archive(path, {"stop_times.txt": FILES["stop_times.txt"].replace(",C,2", ",UNKNOWN,2")})
            with self.assertRaises(ValueError):
                load_gtfs(path, "test", dt.date(2026, 10, 9))

    def test_zip_traversal_rejected(self):
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / "test.zip"
            archive(path, {"../secret.txt": "hello"})
            with self.assertRaises(ValueError):
                load_gtfs(path, "test", dt.date(2026, 10, 9))

    def test_timezone_required(self):
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / "test.zip"
            archive(path, {"agency.txt": FILES["agency.txt"].replace("Europe/Zagreb", "UTC")})
            with self.assertRaises(ValueError):
                load_gtfs(path, "test", dt.date(2026, 10, 9))


if __name__ == "__main__":
    unittest.main()
