# KRENI v0.0.4 — praćenje vozila i precizni polasci

- Android i iOS dobili su jednaku infrastrukturu za verificirane GPS lokacije vozila, odabir stanica i vremena dolaska u minutama.
- Kad prijevoznik dopusti live API, KRENI može prikazati stvarni položaj autobusa, tramvaja ili drugog vozila na karti.
- GPS informacije starije od 90 sekundi odbacuju se, a procjene se razlikuju od planiranih vremena.
- Dodani su testovi valjanosti, ograničenje odgovora i sigurno rukovanje mrežnim greškama.
- Nema vlastite baze podataka i nema lažnih GPS simulacija.

**Trenutačno nijedan službeni produkcijski live feed nije aktiviran.** ZET-ov dostupni GTFS-RT download označen je samo za testiranje, a dozvole za druge prijevoznike tek treba potvrditi. Dolasci u minutama pojavljuju se isključivo kad se validirani izvori priključe.

Izdanje uključuje testno potpisani Android APK, Android AAB bez produkcijskog potpisa, iOS simulator, nepotpisani IPA, XCArchive i zasebne izvorne projekte. Distribucijski certifikati potrebni su za Google Play / App Store.