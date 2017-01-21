#NewsParser
Autor: Aleksei Hemeljainen  
See projekt on Java algkursuse kodutöö  
Programm kirjutatud nullist, ilma abita kasutades google  


NewsParser - programmi pea klass, kus on praegu realiseeritud uudiste parsimine delfi.ee portaalist. Parsitud andmed kuvatakse kasutajaliides.  
NewsParserPreloader - programmmi klass, kus realiseeritud eelkoormus aken  
DB_connector - selles klassis realiseeritud suhtlus andmebaasiga  


Programm hoiab uudiseid antud teema kohta. Programm kuvab uudised mugavas formaadis. Program salvestab uudised. Programm võimaldab otsida infot salvestatud arhiivist.  


Omadused:  
-uudiste parsimine (kategooria, alamkategooria, pealkiri, kuupäev, artikli tekst, pilt)  
-uudiste salvestamine SQLite andmebaasi  
-uudiste kustutamine SQLite andmebaasist  
-uudiste kuvamine (kategooria,alamkategooria, pealkiri, kuupäev, artikli tekst, pilt, kasutaja märkus)  
-uudise pealkiri akna värv muutub - sõltub sellest kas on uudis arhiveeritud või tegemist online uudisega  
-kui interneti ei ole siis sõnum "no internet connection" ilmub aknas (alt navigatsioonimenüü)  
-kui mitte ühtegi artiklit ei ole salsestatu, siis ilmneb sõnum "no items" (üles navigatsioonimenüü)  
-andmete otsimine arhiivist. Otsingu  ajal kontrollitakse kategooria, alamkategooria, pealkiri, kuupäev, artikli tekst, kasutaja märge  
-kasutaja märkuse salvestamine  
-valides artikkel, mis sisaldab märkus, uudis automaatselt kerib alla et oleks kohe näha märkus  
-kui märkus on salvestatud, siis "NOTE:" pealkiri ilmneb aknas  
-vajutades nuppu  "Add/edit note", "NOTE:" pealkiri ilmneb aknas ja uudis automaatselt kerib alla, eelmine märkus kaob tekstist ja ilmneb redigeerimis aknas  
-juhul, kui kasutaja paneb märkuse online artikli, siis artikkel koos märkusega salvestatakse andmebaasi, ja  kuvatakse andmed kohe andmebaasist  
-kui kasutaja salvestab artikkel, siis kuvatakse andmed kohe andmebaasist  
-klikkides online artiklile, mis on juba olemas andmebaasis, suunatakse kohe salvestatud artiklile  
-programmi alguses, uudiste kategooriate ja alamkategooriate parsimise ajal, ilmned teavitus aken



