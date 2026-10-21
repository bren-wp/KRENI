# KRENI — nativni Android razvoj

Projekt sadrži samo Android (Kotlin, Jetpack Compose, MapLibre). Flutter i iOS ne koriste se.

## Lokalno

Android SDK API 36, JDK 17, Gradle 8.14.3:

```bash
cd android
gradle :app:testDebugUnitTest :app:assembleDebug :app:bundleRelease
```

## CI

Workflow .github/workflows/platform-release.yml radi validaciju podataka, Android unit testove, APK/AAB,
test instalacije i pokretanja u Android emulatoru. Na main grani nakon uspjeha objavljuje Android pre-release.

## Integritet podataka

Ne uključivati izvore bez provjerenih uvjeta, ne prikazivati lažne GPS položaje ili procjene i
ne spremati ključeve u repozitorij. Testirati dozvole, offline rad, bateriju, svijetlu i tamnu temu.

## Ograničenja

Za GTFS-RT procjene dolaska potreban je službeni GTFS Static katalog i pouzdano mapiranje ID-jeva.
Nijedan produkcijski live feed još nije aktiviran.
