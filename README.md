<div align="center">

<img src="assets/brand/logo.svg" alt="KRENI — Javni prijevoz Hrvatske" width="480"/>

# Cijela Hrvatska. Jedno putovanje.

**Autobusi · Tramvaji · Vlakovi · Trajekti · Katamarani**

[![Android](https://img.shields.io/badge/Android-Kotlin%20%2B%20Compose-146BFC?logo=android&logoColor=white)](android/)
[![iOS](https://img.shields.io/badge/iOS-Swift%20%2B%20SwiftUI-071936?logo=apple&logoColor=white)](ios/)
[![CI](https://github.com/bren-wp/KRENI/actions/workflows/platform-release.yml/badge.svg)](https://github.com/bren-wp/KRENI/actions/workflows/platform-release.yml)
[![License](https://img.shields.io/badge/license-MIT-02CDBD)](LICENSE)
[![No database](https://img.shields.io/badge/database-none-success)](docs/PRIVACY.md)

**[Preuzimanja](https://github.com/bren-wp/KRENI/releases)** · **[Android](android/)** · **[iOS](ios/)** · **[Izvori podataka](docs/DATA_SOURCES.md)** · **[Privatnost](docs/PRIVACY.md)**

</div>

![KRENI — javni prijevoz Hrvatske](docs/media/hero.svg)

## Jedna aplikacija za hrvatske gradove i otoke

KRENI objedinjuje pregled stanica, kolodvora i luka na jednostavnoj karti. Upoznajte gradski prijevoz, tramvajske mreže Zagreba i Osijeka, vlakove te pomorske linije Jadrana.

### Osmišljeno za jednostavno putovanje

- 🚌 **Autobusi** — lokacije stanica i pristupačan pregled odabranog grada.
- 🚋 **Tramvaji** — gradske mreže Zagreba i Osijeka.
- 🚆 **Vlakovi** — željeznički kolodvori i veze.
- ⛴️ **Trajekti** — luke i pomorski terminali.
- 🚤 **Katamarani** — luka po luka, od obale do otoka.
- ⭐ **Omiljene lokacije** — pohranjene na uređaju.
- 🗺️ **Karta** — MapLibre na Androidu, Apple Maps na iOS-u.
- 🌗 **Teme** — ugodno čitanje danju i noću.
- 🔒 **Privatnost** — nema registracije, vlastite baze podataka ni korisničkog praćenja.

### Prikaz izgleda

<div align="center">
<img src="docs/media/home.svg" alt="Početna stranica — dizajnerska ilustracija" width="225"/>
<img src="docs/media/map.svg" alt="Karta — dizajnerska ilustracija" width="225"/>
<img src="docs/media/settings.svg" alt="Postavke — dizajnerska ilustracija" width="225"/>
</div>

*Ilustracije prikazuju vizualni smjer; nisu potvrđene snimke aplikacija ni stvarni redovi vožnje.*

## Preuzimanja

[**Releases — Android / iOS**](https://github.com/bren-wp/KRENI/releases)

Izdanje `v0.0.2` uključuje zasebne Android i iOS projektne izvore. Distribucijski APK i iOS paket isporučuju se u obliku koji odgovara dostupnim potpisnim certifikatima. **Nepotpisani Android APK i iOS ZIP nisu paketi za neposrednu objavu u trgovinama.** Za instalacijske distribucije potrebni su valjani Android signing ključ i Apple certifikati/provisioning profili. Više u [uputama za izradu](docs/BUILD.md).

## Provjereni podatkovni izvori

KRENI ne smije prikazivati izmišljene linije, vremena ili GPS procjene. Prikazane stanice su orijentacijske; službeni redovi vožnje dostupni su kroz integracije tek nakon potvrde prava korištenja i provjere valjanosti podataka.

- ZET: objavljena otvorena dozvola za GTFS/GTFS-Realtime
- HŽPP: javni GTFS vozni red
- TP Line: javno dostupan GTFS i JSON
- Autotrolej: pronađen GTFS, trenutna dostupnost zahtijeva ponovnu provjeru
- Promet Split, GPP, Jadrolinija, Krilo: javni vozni redovi; status programskog pristupa naveden u [matrici izvora](docs/DATA_SOURCES.md)

KRENI nije službena aplikacija navedenih prijevoznika niti tvrdi da su sve integracije već aktivirane.

## Android i iOS projekti

| Platforma | Izvorni projekt | Sučelje | Pohrana |
| --- | --- | --- | --- |
| Android | [`android/`](android/) | Kotlin, Jetpack Compose | privatna JSON datoteka |
| iOS | [`ios/`](ios/) | Swift, SwiftUI, MapKit | privatna JSON datoteka |

Obje aplikacije koriste istu verzioniranu [datoteku kataloga](data/catalog.json) i jednak naziv/broj izdanja. Nema SQL-a, Firebasea ili Supabasea.

## Pouzdanost voznog reda

U repozitoriju je dostupan [GTFS procesor](pipeline/gtfs.py) koji validira dozvoljene ZIP datoteke, provjerava kalendare, linije, stanice, vremenske zone i dopuštena presjedanja iz potvrđenih grafova. Ne uvozi podatke koji nisu pravno i tehnički provjereni.

Automatizirana provjera pokriva katalog, GTFS procesor te Android/iOS gradnju. Razvojna povezanost ne zamjenjuje testove na fizičkim uređajima.

---

**KRENI © 2026 Brendigo** · [MIT](LICENSE) · [Politika privatnosti](docs/PRIVACY.md)
