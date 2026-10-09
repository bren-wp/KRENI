# KRENI v0.0.5 — pouzdanije praćenje vozila

Ovo izdanje poboljšava stabilnost i sigurnost infrastrukture za vozila i dolaske na Androidu i iOS-u.

- GPS lokacije i procjene dolaska automatski se uklanjaju 90 sekundi nakon zadnjeg valjanog ažuriranja, čak i kada mreža nije dostupna.
- Prikaz se osvježava svakih 10 sekundi; mrežni podaci se dohvaćaju odobrenim izvorima svakih 30 sekundi.
- Spriječeno je zadržavanje podataka iz prethodnog grada nakon promjene lokacije.
- Ispravljena je obrada otkazivanja mrežnih zahtjeva te ekstremnih vremenskih oznaka.
- Jasno se razlikuju „Procjena prijevoznika”, „Planirani polazak” i vrijeme posljednjeg ažuriranja.
- Live izvor mora imati izričito produkcijsko odobrenje i HTTPS poveznicu na uvjete korištenja prije uključivanja.
- Dodani su Android i iOS testovi sigurnosti podataka te provjere izvora.

**Produkcijski live feedovi nisu uključeni.** Ne prikazuju se izmišljeni dolasci, autobusi ili brodovi. Prije aktivacije potreban je provjeren službeni izvor i dopuštenje operatera.

Distribucija uključuje testno potpisani Android APK, AAB i iOS arhive/izvore. Za Google Play i iPhone potrebni su trajni produkcijski certifikati; nepotpisani IPA nije instalacijski paket.
