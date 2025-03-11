# Úprava exportu tabuľky v aplikácii Blocky

Tento návod vám pomôže upraviť export tabuľky v aplikácii Blocky tak, aby výsledná tabuľka vyzerala podobne ako tabuľka vo vašom Word dokumente.

## Úprava funkcie exportu tabuľky

Ak chcete, aby tabuľky exportované z aplikácie Blocky vyzerali rovnako ako tabuľky vo Word dokumente, môžete upraviť funkciu exportu tabuľky v zdrojovom kóde aplikácie.

### 1. Otvorte súbor FolderContentScreen.kt

Tento súbor obsahuje funkcie pre export údajov do rôznych formátov vrátane tabuľky.

### 2. Nájdite funkciu exportToTable

Vyhľadajte funkciu `exportToTable`, ktorá vytvára textový súbor s tabuľkou.

### 3. Upravte formátovanie tabuľky

Nahraďte existujúci kód funkcie `exportToTable` nasledujúcim kódom, ktorý vytvára tabuľku s formátovaním podobným ako vo Word dokumente:

```kotlin
// Funkcia na export súm do textového súboru vo formáte tabuľky
fun exportToTable(fileName: String, title: String, showPreview: Boolean = false): File {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val currentDate = dateFormat.format(Date())
    val exportFile = File(folder, "${fileName}.txt")
    
    val fileContent = StringBuilder()
    
    // Nadpis a dátum
    fileContent.append("${title}\n")
    fileContent.append("Dátum: ${currentDate}\n\n")
    
    // Určenie šírky stĺpcov
    val descriptionWidth = 40  // Šírka stĺpca pre popis
    val amountWidth = 15       // Šírka stĺpca pre sumu
    
    // Vytvorenie hlavičky tabuľky
    fileContent.append("+${"-".repeat(descriptionWidth)}+${"-".repeat(amountWidth)}+\n")
    fileContent.append("| ${"Popis".padEnd(descriptionWidth - 2)} | ${"Suma".padEnd(amountWidth - 2)} |\n")
    fileContent.append("+${"-".repeat(descriptionWidth)}+${"-".repeat(amountWidth)}+\n")
    
    // Pridanie súm z priečinka
    files.forEach { file ->
        if (!file.isDirectory && file.name.contains("€")) {
            val sumRegex = "([\\-]?\\d+[.,]\\d+)€".toRegex()
            val matchResult = sumRegex.find(file.name)
            matchResult?.groupValues?.get(1)?.let { sum ->
                val sumValue = sum.replace(",", ".").toDouble()
                
                // Extrakcia popisu z názvu súboru
                val descriptionRegex = "\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}_(.+)_[\\-]?\\d+[.,]\\d+€".toRegex()
                val descMatch = descriptionRegex.find(file.name)
                var description = descMatch?.groupValues?.get(1)?.replace("_", " ") ?: ""
                if (description == "manual" || description == "receipt") description = ""
                
                // Orezanie popisu, ak je príliš dlhý
                if (description.length > descriptionWidth - 4) {
                    description = description.substring(0, descriptionWidth - 7) + "..."
                }
                
                // Formátovanie riadku tabuľky
                fileContent.append("| ${description.padEnd(descriptionWidth - 2)} | ${String.format("%${amountWidth - 4}s €", String.format("%.2f", sumValue))} |\n")
            }
        }
    }
    
    // Pridanie celkovej sumy na koniec
    fileContent.append("+${"-".repeat(descriptionWidth)}+${"-".repeat(amountWidth)}+\n")
    fileContent.append("| ${"Spolu:".padEnd(descriptionWidth - 2)} | ${String.format("%${amountWidth - 4}s €", String.format("%.2f", folderTotalSum))} |\n")
    fileContent.append("+${"-".repeat(descriptionWidth)}+${"-".repeat(amountWidth)}+\n")
    
    // Pridanie podpisov
    fileContent.append("\n")
    fileContent.append("Podpis odovzdal: _________________________\n")
    fileContent.append("\n")
    fileContent.append("Podpis prijal: ___________________________\n")
    
    // Zápis do súboru
    exportFile.writeText(fileContent.toString())
    
    if (showPreview) {
        previewContent = fileContent.toString()
        previewType = "table"
        showPreviewDialog = true
    } else {
        Toast.makeText(
            context,
            "Sumy boli úspešne exportované do súboru ${fileName}.txt",
            Toast.LENGTH_LONG
        ).show()
    }
    
    return exportFile
}
```

### 4. Upravte funkciu generateFormWithBorders

Ak chcete, aby aj formulár s ohraničením vyzeral podobne ako tabuľka vo Word dokumente, môžete upraviť funkciu `generateFormWithBorders`:

```kotlin
// Funkcia na generovanie formulára s orámovaním
fun generateFormWithBorders(fileName: String, title: String, showPreview: Boolean = false): File {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val currentDate = dateFormat.format(Date())
    val exportFile = File(folder, "${fileName}_formular.txt")
    
    val fileContent = StringBuilder()
    
    // Nadpis a dátum
    fileContent.append("${title}\n")
    fileContent.append("Dátum: ${currentDate}\n\n")
    
    // Určenie šírky stĺpcov a riadkov
    val labelWidth = 20
    val valueWidth = 40
    
    // Hlavička formulára s orámovaním
    fileContent.append("+${"-".repeat(labelWidth)}+${"-".repeat(valueWidth)}+\n")
    fileContent.append("| ${"Meno a priezvisko:".padEnd(labelWidth - 2)} | ${userName.take(valueWidth - 4).padEnd(valueWidth - 2)} |\n")
    fileContent.append("+${"-".repeat(labelWidth)}+${"-".repeat(valueWidth)}+\n")
    fileContent.append("| ${"Zákazník:".padEnd(labelWidth - 2)} | ${folder.name.take(valueWidth - 4).padEnd(valueWidth - 2)} |\n")
    fileContent.append("+${"-".repeat(labelWidth)}+${"-".repeat(valueWidth)}+\n")
    
    // Tabuľka položiek
    fileContent.append("| ${"Položka".padEnd(labelWidth - 2)} | ${"Suma".padEnd(valueWidth - 2)} |\n")
    fileContent.append("+${"-".repeat(labelWidth)}+${"-".repeat(valueWidth)}+\n")
    
    // Pridanie súm z priečinka
    files.forEach { file ->
        if (!file.isDirectory && file.name.contains("€")) {
            val sumRegex = "([\\-]?\\d+[.,]\\d+)€".toRegex()
            val matchResult = sumRegex.find(file.name)
            matchResult?.groupValues?.get(1)?.let { sum ->
                val sumValue = sum.replace(",", ".").toDouble()
                
                // Extrakcia popisu z názvu súboru
                val descriptionRegex = "\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}_(.+)_[\\-]?\\d+[.,]\\d+€".toRegex()
                val descMatch = descriptionRegex.find(file.name)
                var description = descMatch?.groupValues?.get(1)?.replace("_", " ") ?: ""
                if (description == "manual" || description == "receipt") description = ""
                
                // Orezanie popisu, ak je príliš dlhý
                if (description.length > labelWidth - 4) {
                    description = description.substring(0, labelWidth - 7) + "..."
                }
                
                // Formátovanie riadku tabuľky
                fileContent.append("| ${description.padEnd(labelWidth - 2)} | ${String.format("%${valueWidth - 4}s €", String.format("%.2f", sumValue))} |\n")
            }
        }
    }
    
    // Pridanie celkovej sumy
    fileContent.append("+${"-".repeat(labelWidth)}+${"-".repeat(valueWidth)}+\n")
    fileContent.append("| ${"Spolu:".padEnd(labelWidth - 2)} | ${String.format("%${valueWidth - 4}s €", String.format("%.2f", folderTotalSum))} |\n")
    fileContent.append("+${"-".repeat(labelWidth)}+${"-".repeat(valueWidth)}+\n")
    
    // Pridanie podpisov
    fileContent.append("\n")
    fileContent.append("Podpis odovzdal: _________________________\n")
    fileContent.append("\n")
    fileContent.append("Podpis prijal: ___________________________\n")
    
    // Zápis do súboru
    exportFile.writeText(fileContent.toString())
    
    if (showPreview) {
        previewContent = fileContent.toString()
        previewType = "form_with_borders"
        showPreviewDialog = true
    } else {
        Toast.makeText(
            context,
            "Formulár bol úspešne exportovaný do súboru ${fileName}_formular.txt",
            Toast.LENGTH_LONG
        ).show()
    }
    
    return exportFile
}
```

## Úprava vzhľadu tabuľky bez zmeny kódu

Ak nechcete upravovať zdrojový kód aplikácie, môžete použiť nasledujúci postup na vytvorenie tabuľky, ktorá bude vyzerať podobne ako tabuľka vo Word dokumente:

### 1. Exportujte údaje z aplikácie Blocky

1. Otvorte priečinok s položkami v aplikácii Blocky
2. Kliknite na tlačidlo "Export"
3. Vyberte formát "CSV"
4. Zadajte názov súboru a kliknite na "Exportovať"

### 2. Použite skript na formátovanie tabuľky

1. Použite skript `word_to_blocky_formatter.py` na vytvorenie formátovanej tabuľky z vášho Word dokumentu
2. Skopírujte formátovanie tabuľky (ohraničenie, šírka stĺpcov, atď.) z výstupného súboru
3. Manuálne upravte CSV súbor exportovaný z aplikácie Blocky tak, aby používal rovnaké formátovanie

### 3. Alternatívne použite Microsoft Word

1. Otvorte Microsoft Word
2. Vytvorte novú tabuľku s rovnakým formátovaním ako vaša pôvodná tabuľka
3. Skopírujte údaje z aplikácie Blocky do tabuľky vo Word dokumente
4. Uložte dokument vo formáte .docx

## Tipy pre lepšie formátovanie tabuľky

1. **Konzistentná šírka stĺpcov** - Používajte rovnakú šírku stĺpcov pre všetky tabuľky
2. **Zarovnanie čísel** - Čísla v stĺpci so sumami zarovnajte doprava
3. **Ohraničenie tabuľky** - Používajte jednoduché ohraničenie tabuľky pre lepšiu čitateľnosť
4. **Formátovanie hlavičky** - Hlavičku tabuľky zvýraznite tučným písmom alebo odlišnou farbou pozadia
5. **Medzery medzi bunkami** - Pridajte dostatočné medzery medzi textom a ohraničením buniek

## Príklad dobre formátovanej tabuľky

```
+----------------------------------------+----------------+
| Popis položky                          | Suma           |
+----------------------------------------+----------------+
| Nákup materiálu                        |       120,50 € |
| Doprava                                |        35,00 € |
| Práca                                  |       250,00 € |
| Zľava                                  |       -40,00 € |
+----------------------------------------+----------------+
| Spolu:                                 |       365,50 € |
+----------------------------------------+----------------+ 