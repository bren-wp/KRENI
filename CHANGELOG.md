# Promjene

## v0.0.7 — 2026-10-21
- Nativni Kotlin/Compose redizajn pet zaslona i uvodnog zaslona prema zadanim Android UI referencama.
- Material 3 dizajnerski sustav, navigacija s pet kartica i komponentizirane kartice, pretraga, favoriti i izbor grada.
- Karte s odabirom vozila i stajališta, potvrđeni dolasci, planiranje bez izmišljenih ruta.
- Uporaba lokalnih grafičkih motiva uz siguran fallback; nema stvaranja novih slika u aplikaciji.
- Lokalno spremljena postavka prvog pokretanja; kontrole obavijesti deaktivirane bez izvora.
- Optimizacija i nova Android APK/AAB CI konfiguracija.

## v0.0.6 — 2026-10-21
- Android-only, potpuno uklonjen iOS iz koda, CI-a i dokumentacije.
- Stvarni GTFS-Realtime protobuf parser za GPS položaje i kontrolu svježine.
- Popis GPS vozila, izbor vozila i centriranje karte; status bez dostupnog izvora.
- Pauziranje mrežnog pollinga izvan prvog plana i novi testovi.
- Testni ZET feed ostaje isključen dok nema produkcijske verifikacije.

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
