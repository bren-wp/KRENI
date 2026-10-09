# KRENI v0.0.2 — Android i iOS

Jednostavan pregled javnog prijevoza Hrvatske u dvije zasebne aplikacije.

## Android
- Kotlin i Jetpack Compose
- KRENI vizualni identitet, početni ekran, karta, polasci, favoriti i postavke
- Privatna JSON pohrana; bez registracije

## iOS
- Swift i SwiftUI
- Apple Maps, početni ekran, karta, polasci, favoriti i postavke
- Privatna JSON pohrana; bez registracije

## Podatkovni sustav
- Jedinstven verzionirani katalog gradova i stajališta
- GTFS ZIP validacija kalendara, vremenskih zona, identifikatora i presjedanja iz stvarnih podataka
- Jasan izvor i status informacija; bez lažnih procjena dolaska

## Distribucija

Četiri datoteke: Android APK i arhiva projekta te iOS aplikacijska arhiva i arhiva projekta.

**Potpisivanje:** ako GitHub nema konfigurirane potpisne certifikate, isporučeni artefakti ostaju nepotpisani. Android APK nije predviđen za neposrednu instalaciju, a iOS arhiva nije potpisani IPA. Ne objavljuju se kao spremni paketi za Play Store/App Store.

Za promet u stvarnom vremenu, rute i obavijesti potrebni su potvrđeni izvori. Izdanje je početna baza za daljnje proširenje.
