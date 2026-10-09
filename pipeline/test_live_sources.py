import unittest
from pipeline.validate_live_sources import validate, secure_url


class LiveSourceSecurity(unittest.TestCase):
    def test_disallowed_host(self):
        self.assertFalse(secure_url("http://example.org/feed"))
        self.assertFalse(secure_url("https://localhost/feed"))
        self.assertFalse(secure_url("https://127.0.0.1/feed"))
        self.assertFalse(secure_url("https://10.0.0.1/feed"))
        self.assertFalse(secure_url("https://user@example.org/feed"))
        self.assertTrue(secure_url("https://example.org/feed"))

    def test_disabled_missing_approval_rejected(self):
        cfg = {"schemaVersion": 1, "refreshSeconds": 30, "providers": [
            {"id": "zet", "city": "zagreb", "enabled": True,
             "approvedForProduction": False,
             "licenseUrl": "https://example.org/license",
             "endpoint": "https://example.org/feed"}
        ]}
        with self.assertRaises(ValueError):
            validate(cfg, {"zagreb"})

    def test_approved_requires_license(self):
        cfg = {"schemaVersion": 1, "refreshSeconds": 30, "providers": [
            {"id": "zet", "city": "zagreb", "enabled": True,
             "approvedForProduction": True,
             "licenseUrl": None, "endpoint": "https://example.org/feed"}
        ]}
        with self.assertRaises(ValueError):
            validate(cfg, {"zagreb"})

    def test_offline_config_ok(self):
        cfg = {"schemaVersion": 1, "refreshSeconds": 30, "providers": [
            {"id": "zet", "city": "zagreb", "enabled": False,
             "approvedForProduction": False, "licenseUrl": None, "endpoint": None}
        ]}
        validate(cfg, {"zagreb"})

    def test_duplicate_or_unknown_city_rejected(self):
        source = {"id":"zet","city":"zagreb","enabled":False,
                  "approvedForProduction":False,"endpoint":None}
        cfg = {"schemaVersion":1,"refreshSeconds":30,"providers":[source,source]}
        with self.assertRaises(ValueError):
            validate(cfg, {"zagreb"})
        cfg["providers"] = [{"id":"zet","city":"other","enabled":False,
                             "approvedForProduction":False,"endpoint":None}]
        with self.assertRaises(ValueError):
            validate(cfg, {"zagreb"})


if __name__ == "__main__":
    unittest.main()
