# KRENI — izrada i instalacija

## Android

Izvor: [Android projekt](../android/) (Kotlin / Jetpack Compose), verzija 0.0.3.

Na računalu s Android SDK, JDK 17 i Gradle 8.14.3, komande: cd android; gradle :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease :app:bundleRelease.

| Datoteka izdanja | Svrha |
| --- | --- |
| KRENI-Android-v0.0.3-installable-test.apk | Digitalno potpisani APK za testiranje, automatski instaliran na Android emulator |
| KRENI-Android-v0.0.3-release-signed.apk | Distribucijski APK, samo ako su dostupni trajni signing ključevi |
| KRENI-Android-v0.0.3-play-signed.aab | Google Play AAB, samo kad su dostupni trajni signing ključevi |
| KRENI-Android-v0.0.3-unsigned.aab | AAB bez distribucijskog potpisa, za naknadno potpisivanje |
| KRENI-Android-v0.0.3-source.zip | Izvorni Android projekt i zajednički podaci |

AAB se ne instalira kao obični APK. GitHub Secrets nazivi:
- KRENI_ANDROID_KEYSTORE_BASE64 (base64 datoteka JKS)
- KRENI_ANDROID_KEYSTORE_PASSWORD
- KRENI_ANDROID_KEY_ALIAS
- KRENI_ANDROID_KEY_PASSWORD

Bez sve četiri tajne CI isporučuje instalacijski testni APK i nepotpisani AAB. **Testni ključ nije namijenjen produkcijskoj distribuciji** i može se razlikovati između novih buildova. Nadogradnja aplikacije s različitim potpisom može zahtijevati uklanjanje stare instalacije, uz gubitak lokalnih postavki. Trajni produkcijski ključ ostaje izvan repozitorija.

## iOS

Izvor: [iOS projekt](../ios/), Swift / SwiftUI i XcodeGen. Za build su potrebni macOS, Xcode i XcodeGen.

1. Iz korijena repozitorija izvrši: python3 tool/create_app_icons.py
2. U mapi ios izvrši: xcodegen generate
3. Otvori Xcode projekt i odaberi odgovarajući cilj.

| Datoteka izdanja | Svrha |
| --- | --- |
| KRENI-iOS-v0.0.3-source.zip | Xcode projekt i zajednički podaci |
| KRENI-iOS-v0.0.3-simulator.zip | Aplikacija za Apple simulator |
| KRENI-iOS-v0.0.3-unsigned.xcarchive.zip | Uređajska arhiva bez potpisa |
| KRENI-iOS-v0.0.3-unsigned.ipa | Nepotpisana IPA arhiva, ne može se instalirati na fizički iPhone |

Instalacijski iPhone IPA ili TestFlight/App Store distribucija zahtijevaju Apple Developer certifikat i odgovarajući provisioning profil. Dok ih nema, nijedan nepotpisani ZIP/IPA ne predstavlja se kao instalacijski.

## Automatizirana provjera

CI izvršava testove kataloga i GTFS validacije, Gradle testove, Android APK/AAB build, digitalnu provjeru APK-a, instalaciju/pokretanje u emulatoru, iOS kompilaciju za simulator i uređaj, pokretanje u simulatoru te snimanje stvarnih zaslona.

Snimke zaslona nisu umjetno generirane. Nakon provjere njihovog PNG formata, GitHub Actions sprema ih u mapu docs/screenshots i umeće poveznice u README.

## Završni produkcijski QA

Prije javne distribucije potrebno je provesti testove na stvarnim Android i iPhone uređajima, TalkBack/VoiceOver, promjene veličine fontova, rad bez mreže, prijelaze u pozadinu, crash logove, potpise, integracije s prijevoznicima i kartografske licence.
