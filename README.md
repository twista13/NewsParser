#NewsParser

NewsParser - programmi pea klass, kus on praegu realiseeritud uudiste kategoorijate parsimine delfi.ee uudiste portaalist,  kategoorija linkide parsimine, ja need kategooriad kuvatakse kasutajaliideses.

NewsParserPreloader - programmmi klass, kus realiseeritud eelkoormus aken

DB_connector - selles klassis realiseeritud suhtlus andmebaasiga



Programm hoiab uudiseid antud teema kohta. Programm kuvab uudised mugavas formaadis. Program salvestab uudised. Programm võimaldab otsida infot salvestatud arhiivist.

-uudiste parsimine

-uudiste salvestamine andmebaasi SQLite

-uudiste kustutamine

-uudiste kuvamine
- uudise akna värv muutub, sõltub sellest kas on uudis arhiveeritud või tegemist online uudisega
- kui märkus on salvestatud, siis uudis automaatselt kerib alla
- kui märkus on salvestatud, siis "NOTE:" pealkiri ilmneb aknas

-andmete otsimine arhiivist

-märkmete salvestamine
- vajutades nuppu  "Add/edit note", "NOTE:" pealkiri ilmneb aknas
- uudis automaatselt kerib alla
- eelmine märkus kaob tekstist ja ilmneb redigeerimis aknas

-uudiste kategooriate ja alamkategooriate parsimise ajal ilmned teavitus aken

