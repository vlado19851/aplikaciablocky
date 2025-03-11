# Návod na vytvorenie tabuľky v rovnakom formáte ako vo Word dokumente

Tento návod vám pomôže vytvoriť tabuľku v rovnakom formáte ako vo vašom Word dokumente a následne ju použiť v aplikácii Blocky.

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

## Postup vytvorenia formátovanej tabuľky

### 1. Príprava Word dokumentu

Skript dokáže extrahovať údaje a formátovanie z Word dokumentu, ktorý obsahuje tabuľku. Tabuľka by mala mať nasledujúcu štruktúru:
- Prvý riadok: Hlavička tabuľky
- Ďalšie riadky: Údaje
- Posledný stĺpec: Sumy

Príklad tabuľky vo Word dokumente:

| Popis položky | Suma |
|---------------|------|
| Položka 1     | 22,00 € |
| Položka 2     | -10,00 € |

### 2. Spustenie skriptu

1. Otvorte príkazový riadok (cmd) alebo PowerShell
2. Prejdite do priečinka, kde sa nachádza skript:
   ```
   cd cesta\k\priecinku\so\skriptom
   ```
3. Spustite skript s parametrami:
   ```
   python word_to_blocky_formatter.py cesta\k\word_dokumentu.docx cesta\k\vystupnemu_suboru.txt
   ```

Príklad:
```
python word_to_blocky_formatter.py C:\Users\pcdom\Downloads\Nový priečinok (2)\tabuľka.docx C:\Users\pcdom\Downloads\tabulka_formatovana.txt
```

### 3. Výsledok konverzie

Po úspešnej konverzii sa vytvorí textový súbor s tabuľkou vo formáte podobnom ako vo Word dokumente:

```
# Tabuľka vytvorená z dokumentu: tabuľka.docx
# Vytvorené: 10.03.2025 18:30:45
#

Názov tabuľky

+----------------------+----------------+
| Popis položky        | Suma           |
+----------------------+----------------+
| Položka 1            | 22,00 €        |
| Položka 2            | -10,00 €       |
+----------------------+----------------+

Celková suma: 12.00 €

# Informácie pre import do aplikácie Blocky:
# Pre každý riadok tabuľky (okrem hlavičky) vytvorte položku v aplikácii:
# - Popis: Položka 1, Suma: 22.00 €
# - Popis: Položka 2, Suma: -10.00 €
```

### 4. Použitie v aplikácii Blocky

#### A. Manuálne pridanie položiek

1. Otvorte vytvorený textový súbor
2. Pre každú položku uvedenú v sekcii "Informácie pre import do aplikácie Blocky":
   - V aplikácii Blocky otvorte priečinok, do ktorého chcete importovať položky
   - Kliknite na tlačidlo "+" (Pridať sumu ručne)
   - Zadajte popis a sumu podľa údajov v textovom súbore
   - Ak je suma záporná, zaškrtnite políčko "Záporná suma (výdavok)"
   - Kliknite na "Pridať"

#### B. Export tabuľky v aplikácii Blocky

1. Po pridaní všetkých položiek do aplikácie Blocky:
   - Otvorte priečinok s importovanými položkami
   - Kliknite na tlačidlo "Export"
   - Vyberte formát "Tabuľka"
   - Zadajte názov súboru a kliknite na "Exportovať"

2. Výsledná tabuľka v aplikácii Blocky bude mať podobný formát ako pôvodná tabuľka vo Word dokumente.

## Zachovanie formátovania

Skript sa snaží zachovať nasledujúce aspekty formátovania z pôvodnej tabuľky:

1. **Ohraničenie tabuľky** - ak má pôvodná tabuľka ohraničenie, bude mať ohraničenie aj výsledná tabuľka
2. **Šírka stĺpcov** - šírka stĺpcov bude prispôsobená obsahu, podobne ako vo Word dokumente
3. **Formátovanie hlavičky** - hlavička tabuľky bude oddelená od zvyšku tabuľky
4. **Zarovnanie textu** - text bude zarovnaný podobne ako v pôvodnej tabuľke

## Riešenie problémov

### Skript sa nespustí

- Skontrolujte, či máte nainštalovaný Python a či je pridaný do PATH
- Skontrolujte, či máte nainštalovanú knižnicu python-docx

### Skript nenašiel žiadne údaje v tabuľke

- Skontrolujte, či váš Word dokument obsahuje tabuľku
- Uistite sa, že tabuľka má aspoň jeden riadok okrem hlavičky

### Nesprávne formátovanie tabuľky

- Skontrolujte formátovanie tabuľky vo Word dokumente
- Uistite sa, že tabuľka má jednoduchú štruktúru bez zlúčených buniek
- Ak je formátovanie príliš komplexné, skúste zjednodušiť tabuľku vo Word dokumente

## Poznámky

- Skript je navrhnutý pre jednoduché tabuľky s textom a číslami
- Komplexné formátovanie (farby, zlúčené bunky, obrázky) nebude zachované
- Pre najlepšie výsledky používajte jednoduché tabuľky s jasnou štruktúrou 