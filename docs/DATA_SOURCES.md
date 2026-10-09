# KRENI — stanje službenih izvora (9. listopada 2026.)

Podatke koristiti isključivo u skladu s licencijom i dostupnošću. Navedena javna stranica nije automatski dopuštenje za neograničeno preuzimanje ili automatski pristup skrivenim API-jima.

| Izvor | Utvrđeno | Za uključivanje |
| --- | --- | --- |
| ZET, Zagreb | Službena stranica navodi **GTFS Static i GTFS Realtime** pod Otvorenom dozvolom RH: https://www.zet.hr/preuzimanja/odredbe/datoteke-u-gtfs-formatu/669 | Testirati stvarne feed URL-ove, format/protobuf, ažuriranje, atribuciju i kontinuitet |
| HŽPP | GTFS vozni red, dnevna objava: https://www.hzpp.hr/GTFS_files.zip ; https://data.europa.eu/data/datasets/vozni-red-h-putni-kog-prijevoza-u-gtfs-obliku?locale=en | Potvrditi pravila ponovne uporabe, ispravnost ZIP-a i relevantne datume; **GPS nije potvrđen** |
| Autotrolej | Indeksirani GTFS: https://gt.autotrolej.hr/google-transit.zip ; arhiva: https://www.transit.land/feeds/f-u243-autotrolej | Posljednja prijavljena uspješna provjera 11.07.2026., zabilježeni timeouti; provjeriti aktualni endpoint i licenciju izravno |
| Promet Split | Službeni redovi vožnje https://www.promet-split.hr/vozni-red ; službeni sustav praćenja vozila naveden na https://data.gov.hr/aplikacije?dataUsage=APPLICATION | Javni korisnički prikaz nije dokaz prava na korištenje API-ja; potreban ugovor ili službena dokumentacija za API |
| GPP Osijek | Službeni redovi vožnje i tramvajske informacije: https://web.gpp-osijek.com/polasci-tramvaj/ | Strojno čitljiv GTFS ili GTFS-RT izvor i uvjeti ponovne uporabe nisu potvrđeni |
| TP Line | Službeni GTFS i JSON na https://www.tp-line.hr/hr/plovidbeni-red | Potvrditi URL i uvjete, preuzeti datoteku i provjeriti sezonske kalendare |
| Jadrolinija | Službeni redovi plovidbe po linijama: https://www.jadrolinija.hr | Nije verificiran službeni javni GTFS/feed za cjelokupnu mrežu ni dopuštenje za automatizirano preuzimanje |
| Krilo | Redovi plovidbe, otkazi i promjene https://krilo.hr/hr | Nije potvrđen otvoreni strojno čitljiv raspored ni pravo na automatizirani import |
| Agencija za obalni linijski pomorski promet | https://agencija-zolpp.hr/linije-2026/ obuhvaća državne linije i redove plovidbe | Stranica s PDF dokumentima nije sama po sebi GTFS; potrebna provjera pojedinih dokumenata i dopuštenja |

## Obvezne kontrole

1. Svaki GTFS uvoz zahtijeva zapis izvora, dopuštenja i SHA256.
2. GTFS ZIP mora imati valjane agency, stops, routes, trips, stop_times i calendar ili calendar_dates.
3. Identifikatori su izolirani prefiksom prijevoznika kako različiti prijevoznici ne bi dijelili isti stop_id.
4. Sva vremena čuvaju service day i `Europe/Zagreb`, uključujući polaske iza ponoći (`24:00:00` i više).
5. Prikaz "Planirano" odgovara statičkom rasporedu; "Uživo" se dopušta tek s valjanom svježom GPS/GTFS-RT porukom i vremenskom oznakom.
6. Ako se GTFS feed ne može osvježiti ili je istekao, nema izmišljene zamjenske informacije.
7. Offline kopije dopuštenih rasporeda moraju imati vrijeme zadnje sinkronizacije i datum isteka.
8. Kombinacije između prijevoznika zahtijevaju eksplicitno provjerenu vezu između terminala i dovoljno vremena za presjedanje.

## Kartografska podloga

Apple MapKit koristi se na iOS-u. Za Android je MapLibre renderer. Standardni poslužitelji `tile.openstreetmap.org` nisu namijenjeni neograničenom produkcijskom prometu, imaju best-effort kapacitet i zabranjuju offline preuzimanje područja. Potrebno je konfigurirati pružatelja pločica uz dopuštenje i dovoljno kapaciteta. Uvjeti: https://operations.osmfoundation.org/policies/tiles/

Nije dopušteno masovno preuzimati pločice ni glumiti identitet drugog klijenta.
