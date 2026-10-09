# Live informacije KRENI

**Važno:** nema aktivnih ovlaštenih produkcijskih GPS izvora. Nije dopušteno prikazivati izmišljena vozila ili minute dolaska.

ZET-ova službena GTFS dozvola: https://www.zet.hr/preuzimanja/odredbe/datoteke-u-gtfs-formatu/669

ZET-ov portal za GTFS-RT, označen samo za testiranje: https://www.zet.hr/gtfs2

HŽPP objavljuje statički vozni red, ne potvrđeni GPS feed: https://www.hzpp.hr/vozni-red

Autotrolej ima sustav informiranja putnika, ali pristup službenom API-ju i prava redistribucije nisu verificirani.

## Podaci

Stateless HTTPS adapter od odobrenog GTFS-Realtime feeda mora proizvesti JSON sa sljedećim poljima:

- schemaVersion: 1
- city i provider: točni identifikatori autoriziranog izvora
- generatedAt: Unix UTC sekunde
- vehicles: niz zapisa id, route, mode, lat, lon, observedAt
- arrivals: niz zapisa id, stopId, route, mode, scheduledAt, predictedAt, updatedAt

Svako ETA polje mora predstavljati podatak dobiven od izvora, a nikad procjenu iz udaljenosti između GPS koordinata. stopId mora odgovarati službenom mapiranom GTFS identifikatoru stajališta.

Aplikacije osvježavaju podatke svakih 30 sekundi, odbacuju položaje i procjene starije od 90 sekundi, prihvaćaju samo HTTPS i ograničavaju odgovor na 512 KiB. Ne pohranjuju GPS podatke u bazu. Dok izvori iz data/live_sources.json nemaju potvrđenu produkcijsku dozvolu i ispravan endpoint, ostaju isključeni.

## Sigurnost

Zabranjene su neprovjerene API adrese, lažne GPS lokacije i uporaba testnog feeda kao produkcijskog. Prije aktivacije obavezna je provjera dostupnosti, licencije, static/RT GTFS ID mapiranja i opterećenja pružatelja.

Preporuka za svježinu podataka: https://github.com/google/transit/blob/master/gtfs-realtime/best-practices/best-practices.md
