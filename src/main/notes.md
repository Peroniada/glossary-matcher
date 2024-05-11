# Notatki
---

## Różnice między plikami
---

- Format dat:
  - polsko - angielski
	- YY/MM/DD
  - angielsko - francuski 
	- YYYY.MM.DD
- struktura słownika
	- polsko - angielski
		- struktura wyrażenia
          - nazwa angielska / francuska
          - opis po angielsku
          - nazwa polska
          - opis po polsku
        - koniec strony  NATO/PdP JAWNE
        - początek strony NATO/PdP JAWNE nr_strony
    - angielsko - francuski
      - po numerze strony oznaczenie, która litera
      - struktura wyrażenia
        - nazwa francuska / angielska
        - opis po francusku
- numer strony
	- polsko - angielski
		- regex JAWNE [0-9]+  476 - CZĘŚĆ III
	- angielsko - francuski 
      - regex -[0-9]+- 


Plan działania: 
- angielsko - polski 
  - odfiltrować numery stron, stopki itd
  - pogrupować stringi po Literach alfabetu do mapy
  - obserwacja
    - początek definicji posiada wewnątrz stringa ' / ' - to może być początek
    - koniec definicji jest zakończony datą - YY/MM/DD - w angiejskiej
    - koniec definicji polskiej jest oznaczony czymś podobnym - pewnie dokument źródłowy
    - 