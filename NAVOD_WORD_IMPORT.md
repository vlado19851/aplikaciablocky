# Návod na import údajov z Word dokumentu do aplikácie Blocky

Tento návod vám pomôže importovať údaje z Word dokumentu (.docx) do aplikácie Blocky pomocou priloženého Python skriptu a manuálneho pridávania položiek.

## Požiadavky

Pred použitím skriptu potrebujete:

1. **Python 3.6 alebo novší**
   - Stiahnite a nainštalujte z [python.org](https://www.python.org/downloads/)
   - Pri inštalácii zaškrtnite možnosť "Add Python to PATH"

2. **Potrebné knižnice**
   - Otvorte príkazový riadok (cmd) alebo PowerShell
   - Zadajte príkaz:
     ```
     pip install python-docx
     ```

## Postup importu

### 1. Príprava Word dokumentu

Skript dokáže extrahovať údaje z Word dokumentu v nasledujúcich formátoch:

#### A. Údaje v tabuľke
Ak váš dokument obsahuje tabuľku, skript sa pokúsi extrahovať údaje z nej. Tabuľka by mala mať nasledujúcu štruktúru:
- Prvý stĺpec: Popis položky
- Druhý stĺpec: Suma (s alebo bez symbolu €)

Príklad:

| Popis položky | Suma |
|---------------|------|
| Položka 1     | 22,00 € |
| Položka 2     | -10,00 € |

#### B. Údaje v texte
Ak dokument neobsahuje tabuľku, skript sa pokúsi nájsť údaje v texte vo formáte "popis: suma" alebo podobnom formáte.

Príklad:
```
Položka 1: 22,00 €
Položka 2: -10,00 €
```

### 2. Spustenie skriptu

1. Otvorte príkazový riadok (cmd) alebo PowerShell
2. Prejdite do priečinka, kde sa nachádza skript:
   ```
   cd cesta\k\priecinku\so\skriptom
   ```
3. Spustite skript s parametrami:
   ```
   python word_to_blocky.py cesta\k\word_dokumentu.docx cesta\k\vystupnemu_suboru.txt
   ```

Príklad:
```
python word_to_blocky.py C:\Users\pcdom\Downloads\Nový priečinok (2)\tabuľka.docx C:\Users\pcdom\Downloads\tabulka_blocky.txt
```

### 3. Výsledok konverzie

Po úspešnej konverzii sa vytvorí textový súbor s položkami vo formáte:

```
# Položky pre import do aplikácie Blocky
# Vytvorené: 10.03.2025 18:30:45
# Zdrojový súbor: tabuľka.docx
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
- Skontrolujte, či máte nainštalovanú knižnicu python-docx

### Skript nenašiel žiadne položky

- Skontrolujte formát vášho Word dokumentu
- Uistite sa, že dokument obsahuje tabuľku alebo text vo formáte "popis: suma"
- Skúste upraviť dokument tak, aby zodpovedal jednému z podporovaných formátov

### Nesprávne extrahované hodnoty

- Skontrolujte formát čísel v Word dokumente
- Uistite sa, že používate správny desatinný oddeľovač (bodka alebo čiarka)
- Skontrolujte, či sumy obsahujú len číselné hodnoty a symbol meny (€)

## Poznámky

- Skript predpokladá, že prvý stĺpec tabuľky obsahuje popis a druhý stĺpec obsahuje sumu
- Ak váš Word dokument má inú štruktúru, upravte skript podľa potreby
- Pre väčšie množstvo položiek zvážte implementáciu priameho importu Word dokumentov v aplikácii Blocky podľa podobného princípu ako pre Excel súbory v súbore `IMPLEMENTACIA_IMPORTU.md` 