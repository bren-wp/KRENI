# KRENI — izrada i instalacija

## Android

Izvor: [Android projekt](../android/) (Kotlin / Jetpack Compose), verzija 0.0.6.

Na računalu s Android SDK, JDK 17 i Gradle 8.14.3, komande: cd android; gradle :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease :app:bundleRelease.

| Datoteka izdanja | Svrha |
| --- | --- |
| KRENI-Android-v0.0.6-installable-test.apk | Digitalno potpisani APK za testiranje, automatski instaliran na Android emulator |
| KRENI-Android-v0.0.6-release-signed.apk | Distribucijski APK, samo ako su dostupni trajni signing ključevi |
| KRENI-Android-v0.0.6-play-signed.aab | Google Play AAB, samo kad su dostupni trajni signing ključevi |
| KRENI-Android-v0.0.6-unsigned.aab | AAB bez distribucijskog potpisa, za naknadno potpisivanje |
| KRENI-Android-v0.0.6-source.zip | Izvorni Android projekt i zajednički podaci |

AAB se ne instalira kao obični APK. GitHub Secrets nazivi:
- KRENI_ANDROID_KEYSTORE_BASE64 (base64 datoteka JKS)
- KRENI_ANDROID_KEYSTORE_PASSWORD
- KRENI_ANDROID_KEY_ALIAS
- KRENI_ANDROID_KEY_PASSWORD

Bez sve četiri tajne CI isporučuje instalacijski testni APK i nepotpisani AAB. **Testni ključ nije namijenjen produkcijskoj distribuciji** i može se razlikovati između novih buildova. Nadogradnja aplikacije s različitim potpisom može zahtijevati uklanjanje stare instalacije, uz gubitak lokalnih postavki. Trajni produkcijski ključ ostaje izvan repozitorija.

## Automatizirana provjera

CI izvršava Python validaciju kataloga i live izvora, JVM testove, Android APK/AAB build i provjeru APK potpisa te instalaciju i snimanje četiri zaslona Android emulatora.

Snimke Android emulatora nastaju u CI-u i spremaju se kao workflow artefakti.

## Završni produkcijski QA

Prije javne distribucije potrebno je provesti testove na stvarnim Android uređajima, TalkBack, promjene veličine fontova, rad bez mreže, prijelaze u pozadinu, crash logove, potpise, integracije s prijevoznicima i kartografske licence.
