# Privatnost

KRENI ne zahtijeva registraciju i ne koristi vlastitu bazu podataka, Firebase, Supabase, analitičke SDK-ove ili oglasne identifikatore.

- Izbor grada, tema i spremljene lokacije ostaju u privatnoj JSON datoteci uređaja.
- Kartografske aplikacije mogu razmjenjivati mrežne zahtjeve s pružateljem karte. KRENI ne zahtijeva pristup GPS lokaciji uređaja.
- Na Androidu karta koristi MapLibre, a na iOS-u MapKit. Prije produkcijske distribucije potrebno je potvrditi vanjske uvjete pružatelja pločica.
- KRENI ne prikuplja ime, adresu e-pošte, telefonski broj ni podatke za plaćanje.
- Brisanjem podataka aplikacije brišu se lokalne postavke i favoriti.
- Buduće integracije za vozne redove moraju biti HTTPS i s valjanim uvjetima korištenja.
