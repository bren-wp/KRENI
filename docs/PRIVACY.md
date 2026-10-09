# Privatnost KRENI

KRENI v0.0.1 ne zahtijeva prijavu i ne koristi vlastitu bazu podataka, analitiku niti oglasne identifikatore.

- **Lokalno:** odabrani grad, tamna tema i favoriti spremaju se u privatnu JSON datoteku aplikacije.
- **Mreža:** prikaz karte učitava pločice s OpenStreetMap poslužitelja, pri čemu mrežni poslužitelj može obrađivati IP adresu i tehničke podatke o zahtjevu prema vlastitim pravilima.
- **Budući feed:** ako razvojni graditelj uključi HTTPS izvor voznih redova, aplikacija komunicira s tim vanjskim izvorom.
- **Lokacija uređaja:** v0.0.1 ne traži GPS dozvolu ni pozadinsko praćenje lokacije.
- **Brisanje:** brisanjem podataka aplikacije uklanja se lokalni JSON. Unutar aplikacije favoriti se mogu ukloniti pojedinačno.

Za produkcijsku objavu potrebno je proširiti obavijest prema stvarno aktiviranim integracijama i pravilima pružatelja usluga.
