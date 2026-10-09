# Razvoj i izdavanje KRENI

## Lokalni razvoj

```bash
flutter create --project-name kreni --org hr.brendigo --platforms android,ios .
flutter pub get
flutter analyze
flutter test
flutter run
```

Bez vlastite baze podataka ili backend konfiguracije.

## GitHub Actions

`release.yml` izvršava analizu i testove na Linuxu, Android release APK build na Linuxu i nepotpisani iOS build na macOS-u. Release korak radi isključivo nakon uspjeha sva tri posla na `main` te stvara `v0.0.1` kao **pre-release**. iOS ZIP nije instalacijski IPA i ne može se objaviti u App Storeu bez Apple certifikata/provisioning profila.

## Politika kvalitete

- Nema lažnih polazaka, kašnjenja niti GPS položaja.
- Nema tokena, lozinki ni signing ključeva u repozitoriju.
- Novi vanjski API mora biti HTTPS i pravno dopušten.
- Ne označavati izdanje kao stabilno dok nije testirano na fizičkim Android/iOS uređajima.
- Izgled i funkcionalnost uvijek treba provjeriti za svijetlu i tamnu temu.

## Versioning

Prvo izdanje `v0.0.1` namijenjeno je razvoju i demonstraciji sučelja. Sljedeće verzije trebaju slijediti stvarni opseg implementiranih promjena.
