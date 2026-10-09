# Izrada Android i iOS paketa

Obje aplikacije imaju vlastiti izvorni projekt, bez zajedničkog runtime okruženja i baze podataka.

## Android

Zahtijeva Java 17, Android SDK i Gradle 8.14.3.

```bash
cd android
gradle :app:testDebugUnitTest :app:assembleRelease
```

Gradle bez konfiguriranog release ključa izrađuje **nepotpisani APK**. Za stvarnu distribuciju treba konfigurirati trajni release keystore i signingConfig putem sigurnih GitHub Secrets, bez pohranjivanja ključeva u repozitorij.

## iOS

Zahtijeva macOS, Xcode i XcodeGen.

```bash
cd ios
xcodegen generate
xcodebuild -project KRENI.xcodeproj -scheme KRENI -configuration Release \
  -sdk iphoneos -destination 'generic/platform=iOS' CODE_SIGNING_ALLOWED=NO build
```

Bez Apple Distribution certifikata/provisioning profila rezultat se ne može distribuirati kao instalacijski IPA u App Storeu. GitHub objavljuje ZIP nepotpisane aplikacije i izvorni iOS projekt.

## Podatkovne provjere

```bash
python3 pipeline/validate_catalog.py
python3 -m unittest discover -s pipeline -p 'test_*.py' -v
python3 -m pipeline.gtfs approved-feed.zip --provider provider-id --date 2026-10-09 --output schedule.json
```

Import je namijenjen isključivo arhivama s potvrđenim pravom uporabe i važećim kalendarom. Ne sprema se SQL baza.

## Testiranje prije objave u trgovinama

- Potvrditi sigurno pokretanje na stvarnim Android/iOS uređajima.
- Provjeriti offline ponašanje, manji/veći zaslon, VoiceOver/TalkBack, tamnu temu i pristupačnost.
- Provjeriti zahtjeve za obavijesti tek nakon uključivanja pouzdanih izvora; nema generičkih obavijesti.
- Potvrditi certifikate, potpise, identitete aplikacija, ikone, zaslone i crash logove.
- Prije produkcijskog skaliranja provjeriti ugovor i kapacitet kartografskog pružatelja.
