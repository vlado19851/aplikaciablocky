# Návod na použitie Python skriptu pre konverziu Excel súboru

Tento návod vám pomôže použiť priložený Python skript `excel_to_blocky.py` na konverziu Excel súboru do formátu, ktorý sa dá ľahšie importovať do aplikácie Blocky.

## Požiadavky

Pred použitím skriptu potrebujete:

1. **Python 3.6 alebo novší**
   - Stiahnite a nainštalujte z [python.org](https://www.python.org/downloads/)
   - Pri inštalácii zaškrtnite možnosť "Add Python to PATH"

2. **Potrebné knižnice**
   - Otvorte príkazový riadok (cmd) alebo PowerShell
   - Zadajte príkaz:
     ```
     pip install pandas openpyxl
     ```

## Postup konverzie

### 1. Príprava Excel súboru

1. Otvorte váš Excel súbor
2. Uistite sa, že:
   - Prvý stĺpec obsahuje popis položiek
   - Druhý stĺpec obsahuje sumy
   - Prvý riadok obsahuje hlavičky stĺpcov

Príklad správneho formátu:

| Popis položky | Suma |
|---------------|------|
| Položka 1     | 22,00|
| Položka 2     |-10,00|

### 2. Spustenie skriptu

1. Otvorte príkazový riadok (cmd) alebo PowerShell
2. Prejdite do priečinka, kde sa nachádza skript:
   ```
   cd cesta\k\priecinku\so\skriptom
   ```
3. Spustite skript s parametrami:
   ```
   python excel_to_blocky.py cesta\k\excel_suboru.xlsx cesta\k\vystupnemu_suboru.txt
   ```

Príklad:
```
python excel_to_blocky.py C:\Users\pcdom\Downloads\tabulka.xlsx C:\Users\pcdom\Downloads\tabulka_blocky.txt
```

### 3. Výsledok konverzie

Po úspešnej konverzii sa vytvorí textový súbor s položkami vo formáte:

```
# Položky pre import do aplikácie Blocky
# Vytvorené: 10.03.2025 18:30:45
# Zdrojový súbor: tabulka.xlsx
#
# Formát: Popis | Suma
# Záporné sumy sú označené znamienkom mínus (-)
#
Položka 1 | 22,00
Položka 2 | -10,00
#
# Celková suma: 12.00
# Počet položiek: 2
```

### 4. Import do aplikácie Blocky

1. Otvorte vytvorený textový súbor
2. Pre každú položku v súbore:
   - V aplikácii Blocky otvorte priečinok, do ktorého chcete importovať položky
   - Kliknite na tlačidlo "+" (Pridať sumu ručne)
   - Zadajte popis a sumu podľa údajov v textovom súbore
   - Ak je suma záporná, zaškrtnite políčko "Záporná suma (výdavok)"
   - Kliknite na "Pridať"

## Riešenie problémov

### Skript sa nespustí

- Skontrolujte, či máte nainštalovaný Python a či je pridaný do PATH
- Skontrolujte, či máte nainštalované potrebné knižnice (pandas, openpyxl)

### Chyba pri konverzii

- Skontrolujte formát vášho Excel súboru
- Uistite sa, že sumy sú číselné hodnoty
- Skontrolujte, či máte prístupové práva k zadaným súborom

### Nesprávne konvertované hodnoty

- Skontrolujte formát čísel v Excel súbore
- Uistite sa, že používate správny desatinný oddeľovač (bodka alebo čiarka)

## Poznámky

- Skript predpokladá, že prvý stĺpec obsahuje popis a druhý stĺpec obsahuje sumu
- Ak váš Excel súbor má inú štruktúru, upravte skript podľa potreby
- Pre väčšie množstvo položiek zvážte implementáciu priameho importu Excel súborov v aplikácii Blocky podľa návodu v súbore `IMPLEMENTACIA_IMPORTU.md` 