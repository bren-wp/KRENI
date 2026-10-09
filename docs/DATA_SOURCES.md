# Izvori podataka i integracije

KRENI ne koristi vlastitu bazu podataka. Trenutačni katalog lokacija nalazi se u `lib/models/transit.dart` i služi za snalaženje, a ne za prikaz službenih redova vožnje.

## Plan

1. Potvrditi javnu dostupnost i licencu podataka kod svakog prijevoznika.
2. Podržati službeni statički GTFS (stanice/linije/kalendar), uz provjeru vremenske zone Europe/Zagreb i promjena ljetnog/zimskog računanja vremena.
3. Podržati GTFS-Realtime samo gdje postoji stvarni endpoint i dopuštenje.
4. Za pomorski promet razlikovati planirani polazak, vremenske izmjene i otkazivanja.
5. Omogućiti označene offline kopije redova vožnje s vremenom zadnje sinkronizacije; nikada ne glumiti GPS uživo.
6. Provjeriti dostupnost službenih izvora HŽPP, ZET, Autotrolej, Promet Split, GPP, Jadrolinija, TP Line i drugih.
7. Validirati jedinstvene identifikatore stajališta, metapodatke izvora, verziju, rok valjanosti i licence prije uključivanja u aplikaciju.

## Postojeći JSON adapter

`DepartureFeed` prihvaća samo HTTPS i JSON objekt s nizom `departures` (maksimalno 10.000 zapisa/2 MiB). Svaki zapis mora imati:
- `stop_id`: poznati identifikator stanice
- `line`: oznaka linije
- `destination`: odredište
- `departure_time`: ISO 8601 s vremenskom zonom
- `source`: naziv izvora

Povezivanje: `--dart-define=KRENI_FEED_URL=https://.../approved.json`.

**Nema konfiguriranog službenog izvora** u repozitoriju. Nije dopušteno preuzimanje nedozvoljenih ili privatnih API-ja, prikaz planiranih vremena kao 'uživo' ili predstavljanje projekta kao službene aplikacije prijevoznika.

Za produkcijsko korištenje OpenStreetMap pločica treba procijeniti kapacitet i odabrati odgovarajućeg pružatelja kartografske podloge sukladno njegovim uvjetima.
