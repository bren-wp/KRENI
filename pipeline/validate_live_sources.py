"""Check live transport configuration without reaching third-party endpoints."""
import ipaddress
import json
from pathlib import Path
from urllib.parse import urlsplit

CONFIG = Path(__file__).resolve().parents[1] / "data/live_sources.json"
CATALOG = Path(__file__).resolve().parents[1] / "data/catalog.json"


def secure_url(url: str) -> bool:
    if not isinstance(url, str) or not url:
        return False
    try:
        parsed = urlsplit(url)
        if parsed.scheme != "https" or not parsed.hostname or parsed.port not in (None, 443):
            return False
        if parsed.username or parsed.password or parsed.fragment:
            return False
        if parsed.hostname == "localhost" or parsed.hostname.endswith(".localhost"):
            return False
        try:
            address = ipaddress.ip_address(parsed.hostname)
            if not address.is_global:
                return False
        except ValueError:
            pass
        return True
    except ValueError:
        return False


def validate(config: dict, cities: set[str]) -> None:
    if config.get("schemaVersion") != 1:
        raise ValueError("Unsupported live config schema")
    refresh = config.get("refreshSeconds")
    if not isinstance(refresh, int) or not 20 <= refresh <= 120:
        raise ValueError("Invalid polling interval")
    providers = config.get("providers")
    if not isinstance(providers, list):
        raise ValueError("Invalid providers")
    seen = set()
    for source in providers:
        if not isinstance(source, dict):
            raise ValueError("Provider entry must be an object")
        identity = (source.get("id"), source.get("city"))
        if not all(isinstance(x, str) and x for x in identity):
            raise ValueError("Invalid provider identity")
        if identity in seen:
            raise ValueError("Duplicate provider in one city")
        seen.add(identity)
        if source["city"] not in cities:
            raise ValueError("Unknown city")
        if not isinstance(source.get("enabled"), bool):
            raise ValueError("Enabled must be boolean")
        if not isinstance(source.get("approvedForProduction"), bool):
            raise ValueError("Missing explicit production approval")
        if source["enabled"]:
            if not source["approvedForProduction"]:
                raise ValueError("A non-production provider cannot be enabled")
            if not secure_url(source.get("endpoint")):
                raise ValueError("Enabled provider must have public HTTPS endpoint")
            if not secure_url(source.get("licenseUrl")):
                raise ValueError("Enabled provider needs a public HTTPS license")
        if source.get("endpoint") is not None and not secure_url(source["endpoint"]):
            raise ValueError("Forbidden provider endpoint")


def main() -> None:
    with CONFIG.open(encoding="utf-8") as fp:
        sources = json.load(fp)
    with CATALOG.open(encoding="utf-8") as fp:
        catalog = json.load(fp)
    validate(sources, {c["id"] for c in catalog["cities"]})
    print(f"Live source configuration valid: {len(sources['providers'])} providers")


if __name__ == "__main__":
    main()
