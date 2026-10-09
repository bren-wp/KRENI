# Promjene

## v0.0.5 — 2026-10-10
- Odvojeno desetsekundno osvježavanje prikaza na Androidu i iOS-u, neovisno o mrežnom pollingu.
- Uklanjanje isteklih GPS položaja i procjena dolaska nakon 90 sekundi.
- Izolacija prikaza pri promjeni grada i sigurno otkazivanje mrežnih zahtjeva.
- Sprječavanje preljeva cjelobrojnih vremenskih oznaka i padova aplikacije zbog neispravnog feeda.
- Jasna oznaka procjene prijevoznika, planiranog polaska i vremena zadnjeg ažuriranja.
- Zaštita od aktivacije live izvora bez eksplicitne potvrde produkcijske dozvole.
- Obvezne provjere konfiguracije i izvršavanje iOS XCTest testova.
- Nema vlastite baze podataka ni simuliranih vozila.

## v0.0.4 — 2026-10-09
- Zajednički sloj za praćenje vozila i minute dolaska u Android i iOS aplikaciji.
- Kartografski markeri za verified GPS položaje, kontrola svježine 90 sekundi.
- Precizno razlikovanje procjene od planiranog polaska, bez izmišljanja procjena.
- Siguran HTTPS snapshot adapter i zaštita od zastarjelih podataka.
- QA i osvježeni stvarni prikazi obiju platformi.
- Izvori bez potvrđenih produkcijskih dozvola ostaju isključeni.

## v0.0.3 — 2026-10-09
- Digitalno potpisani testni Android APK i provjera instalacije u emulatoru.
- Android AAB te opcionalno trajno produkcijsko potpisivanje.
- iOS simulator, uređajska xcarchive arhiva, nepotpisani IPA.
- iOS KRENI ikona i početni zaslon.
- Stvarne snimke zaslona obiju aplikacija za README.
- Ujednačene verzije i provjere preuzimanja prije GitHub izdanja.

## v0.0.2 — 2026-10-09
- Android Kotlin/Compose i iOS Swift/SwiftUI projekti.
- Katalog stanica, lokalna JSON pohrana i GTFS validacija.

## v0.0.1 — 2026-10-09
- Početna verzija projekta KRENI.
