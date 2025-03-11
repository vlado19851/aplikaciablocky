# Návod na implementáciu exportu do Word dokumentu v aplikácii Blocky

Tento návod vám pomôže implementovať funkciu exportu údajov priamo do Word dokumentu (.docx) v aplikácii Blocky. Táto funkcia umožní používateľom vytvárať profesionálne vyzerajúce dokumenty priamo z aplikácie.

## Kroky implementácie

### 1. Pridajte potrebné závislosti

Do súboru `build.gradle` (app) pridajte nasledujúce závislosti:

```gradle
dependencies {
    // Existujúce závislosti
    
    // Apache POI pre prácu s Word dokumentmi
    implementation 'org.apache.poi:poi:5.2.3'
    implementation 'org.apache.poi:poi-ooxml:5.2.3'
}
```

Po pridaní závislostí nezabudnite synchronizovať projekt (kliknite na "Sync Now").

### 2. Vytvorte triedu WordExporter

Vytvorte nový súbor `WordExporter.kt` v balíčku `com.example.aplikaciablocky` a pridajte do neho nasledujúci kód:

```kotlin
package com.example.aplikaciablocky

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import org.apache.poi.xwpf.usermodel.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Trieda pre export údajov do Word dokumentu (.docx)
 */
class WordExporter(private val context: Context) {

    /**
     * Exportuje údaje z priečinka do Word dokumentu
     * @param folder Priečinok s údajmi
     * @param fileName Názov výstupného súboru (bez prípony)
     * @param title Nadpis dokumentu
     * @return Uri výstupného súboru alebo null v prípade chyby
     */
    fun exportToWord(folder: File, fileName: String, title: String): Uri? {
        try {
            // Vytvorenie Word dokumentu
            val document = XWPFDocument()
            
            // Pridanie nadpisu
            val titleParagraph = document.createParagraph()
            titleParagraph.alignment = ParagraphAlignment.CENTER
            val titleRun = titleParagraph.createRun()
            titleRun.setText(title)
            titleRun.isBold = true
            titleRun.fontSize = 16
            
            // Pridanie dátumu
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val currentDate = dateFormat.format(Date())
            val dateParagraph = document.createParagraph()
            dateParagraph.alignment = ParagraphAlignment.LEFT
            val dateRun = dateParagraph.createRun()
            dateRun.setText("Dátum: $currentDate")
            
            // Pridanie prázdneho riadku
            document.createParagraph()
            
            // Vytvorenie tabuľky
            val table = document.createTable(1, 2)
            
            // Nastavenie šírky stĺpcov
            val tableWidth = 9000 // Celková šírka tabuľky v twips (1/20 bodu)
            val descriptionWidth = (tableWidth * 0.7).toInt() // 70% šírky pre popis
            val amountWidth = tableWidth - descriptionWidth // 30% šírky pre sumu
            
            // Nastavenie štýlu tabuľky
            val tableStyle = "TableGrid"
            table.setWidth(tableWidth.toString())
            
            // Vytvorenie hlavičky tabuľky
            val headerRow = table.getRow(0)
            val headerCell1 = headerRow.getCell(0)
            headerCell1.setText("Popis")
            headerCell1.color = "D3D3D3" // Svetlo šedá farba pozadia
            
            val headerCell2 = headerRow.getCell(1)
            headerCell2.setText("Suma")
            headerCell2.color = "D3D3D3" // Svetlo šedá farba pozadia
            
            // Nastavenie tučného písma pre hlavičku
            headerCell1.paragraphs.forEach { paragraph ->
                paragraph.runs.forEach { run ->
                    run.isBold = true
                }
            }
            
            headerCell2.paragraphs.forEach { paragraph ->
                paragraph.runs.forEach { run ->
                    run.isBold = true
                }
            }
            
            // Získanie údajov z priečinka
            val items = mutableListOf<Pair<String, Double>>()
            var folderTotalSum = 0.0
            
            folder.listFiles()?.forEach { file ->
                if (!file.isDirectory && file.name.contains("€")) {
                    val sumRegex = "([\\-]?\\d+[.,]\\d+)€".toRegex()
                    val matchResult = sumRegex.find(file.name)
                    matchResult?.groupValues?.get(1)?.let { sum ->
                        val sumValue = sum.replace(",", ".").toDouble()
                        folderTotalSum += sumValue
                        
                        // Extrakcia popisu z názvu súboru
                        val descriptionRegex = "\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}_(.+)_[\\-]?\\d+[.,]\\d+€".toRegex()
                        val descMatch = descriptionRegex.find(file.name)
                        var description = descMatch?.groupValues?.get(1)?.replace("_", " ") ?: ""
                        if (description == "manual" || description == "receipt") description = ""
                        
                        items.add(Pair(description, sumValue))
                    }
                }
            }
            
            // Pridanie položiek do tabuľky
            items.forEach { (description, amount) ->
                val row = table.createRow()
                val cell1 = row.getCell(0)
                cell1.setText(description)
                
                val cell2 = row.getCell(1)
                cell2.setText(String.format("%.2f €", amount))
                
                // Zarovnanie sumy doprava
                cell2.paragraphs.forEach { paragraph ->
                    paragraph.alignment = ParagraphAlignment.RIGHT
                }
            }
            
            // Pridanie riadku s celkovou sumou
            val totalRow = table.createRow()
            val totalCell1 = totalRow.getCell(0)
            totalCell1.setText("Spolu:")
            totalCell1.paragraphs.forEach { paragraph ->
                paragraph.runs.forEach { run ->
                    run.isBold = true
                }
            }
            
            val totalCell2 = totalRow.getCell(1)
            totalCell2.setText(String.format("%.2f €", folderTotalSum))
            totalCell2.paragraphs.forEach { paragraph ->
                paragraph.alignment = ParagraphAlignment.RIGHT
                paragraph.runs.forEach { run ->
                    run.isBold = true
                }
            }
            
            // Pridanie podpisov
            document.createParagraph() // Prázdny riadok
            
            val signatureParagraph1 = document.createParagraph()
            val signatureRun1 = signatureParagraph1.createRun()
            signatureRun1.setText("Podpis odovzdal: _________________________")
            
            document.createParagraph() // Prázdny riadok
            
            val signatureParagraph2 = document.createParagraph()
            val signatureRun2 = signatureParagraph2.createRun()
            signatureRun2.setText("Podpis prijal: ___________________________")
            
            // Uloženie dokumentu
            val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val outputFile = File(outputDir, "$fileName.docx")
            val outputStream = FileOutputStream(outputFile)
            document.write(outputStream)
            outputStream.close()
            
            // Zobrazenie hlásenia o úspešnom exporte
            Toast.makeText(
                context,
                "Údaje boli úspešne exportované do súboru $fileName.docx",
                Toast.LENGTH_LONG
            ).show()
            
            // Vrátenie Uri súboru
            return Uri.fromFile(outputFile)
            
        } catch (e: Exception) {
            // Zobrazenie chybového hlásenia
            Toast.makeText(
                context,
                "Chyba pri exporte do Word dokumentu: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            e.printStackTrace()
            return null
        }
    }
}
```

### 3. Pridajte funkciu exportToWord do triedy FolderContentScreen

Do súboru `FolderContentScreen.kt` pridajte nasledujúcu funkciu:

```kotlin
/**
 * Funkcia pre export údajov do Word dokumentu
 */
fun exportToWord(fileName: String, title: String): Uri? {
    try {
        val wordExporter = WordExporter(context)
        return wordExporter.exportToWord(folder, fileName, title)
    } catch (e: Exception) {
        Toast.makeText(
            context,
            "Chyba pri exporte do Word dokumentu: ${e.message}",
            Toast.LENGTH_LONG
        ).show()
        return null
    }
}
```

### 4. Pridajte tlačidlo pre export do Word dokumentu

V existujúcom dialógu pre export v súbore `FolderContentScreen.kt` pridajte tlačidlo pre export do Word dokumentu:

```kotlin
// Tlačidlo pre export do Word dokumentu
Button(
    onClick = {
        if (exportFileName.isNotBlank()) {
            try {
                val uri = exportToWord(exportFileName, exportTitle)
                if (uri != null) {
                    exportedFile = File(uri.path ?: "")
                    showExportDialog = false
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Chyba pri exporte: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                context,
                "Zadajte názov súboru",
                Toast.LENGTH_SHORT
            ).show()
        }
    },
    modifier = Modifier.padding(4.dp)
) {
    Text("Word (.docx)")
}
```

Toto tlačidlo by malo byť pridané do sekcie s ostatnými tlačidlami pre export, napríklad:

```kotlin
// Sekcia s tlačidlami pre export
Row(
    horizontalArrangement = Arrangement.SpaceEvenly,
    modifier = Modifier.fillMaxWidth()
) {
    // Existujúce tlačidlá pre export
    
    // Nové tlačidlo pre export do Word dokumentu
    Button(
        onClick = {
            if (exportFileName.isNotBlank()) {
                try {
                    val uri = exportToWord(exportFileName, exportTitle)
                    if (uri != null) {
                        exportedFile = File(uri.path ?: "")
                        showExportDialog = false
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        "Chyba pri exporte: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    context,
                    "Zadajte názov súboru",
                    Toast.LENGTH_SHORT
                ).show()
            }
        },
        modifier = Modifier.padding(4.dp)
    ) {
        Text("Word (.docx)")
    }
}
```

### 5. Pridajte potrebné importy

Do súboru `FolderContentScreen.kt` pridajte nasledujúce importy (ak ešte nie sú pridané):

```kotlin
import android.net.Uri
import android.os.Environment
import org.apache.poi.xwpf.usermodel.*
import java.io.FileOutputStream
```

## Testovanie implementácie

Po implementácii všetkých zmien by ste mali mať funkčné tlačidlo pre export do Word dokumentu.

### Testovanie exportu do Word dokumentu

1. Spustite aplikáciu Blocky
2. Otvorte priečinok s položkami
3. Kliknite na tlačidlo "Export"
4. Zadajte názov súboru a nadpis
5. Kliknite na tlačidlo "Word (.docx)"
6. Skontrolujte, či sa vytvoril Word dokument v priečinku Documents vášho zariadenia
7. Otvorte vytvorený dokument a skontrolujte, či obsahuje všetky údaje a či je správne formátovaný

## Riešenie problémov

### Chyba pri kompilácii

- Skontrolujte, či ste správne pridali závislosti do súboru `build.gradle`
- Uistite sa, že ste synchronizovali projekt po pridaní závislostí

### Chyba pri exporte

- Skontrolujte, či máte povolenia na zápis do priečinka Documents
- Uistite sa, že máte dostatočné miesto na úložisku zariadenia

### Nesprávne formátovanie dokumentu

- Skontrolujte kód v triede `WordExporter`
- Uistite sa, že používate správne metódy pre formátovanie textu a tabuľky

## Prispôsobenie vzhľadu Word dokumentu

Ak chcete prispôsobiť vzhľad Word dokumentu, môžete upraviť kód v triede `WordExporter`. Tu sú niektoré možnosti prispôsobenia:

### Zmena veľkosti a štýlu písma

```kotlin
// Zmena veľkosti písma
titleRun.fontSize = 20 // Väčšie písmo pre nadpis

// Zmena štýlu písma
titleRun.setFontFamily("Arial") // Zmena fontu na Arial
```

### Zmena farby textu

```kotlin
// Zmena farby textu
titleRun.setColor("FF0000") // Červená farba pre nadpis (RGB v hexadecimálnom formáte)
```

### Zmena štýlu tabuľky

```kotlin
// Zmena štýlu tabuľky
table.setStyle("LightShading-Accent1") // Použitie preddefinovaného štýlu tabuľky
```

### Pridanie loga alebo obrázka

```kotlin
// Pridanie obrázka
val logoParagraph = document.createParagraph()
val logoRun = logoParagraph.createRun()
logoRun.addPicture(
    FileInputStream(File(context.getExternalFilesDir(null), "logo.png")),
    XWPFDocument.PICTURE_TYPE_PNG,
    "logo.png",
    Units.toEMU(100.0), // Šírka v EMU (English Metric Units)
    Units.toEMU(50.0)   // Výška v EMU
)
```

## Ďalšie možnosti

### Pridanie možnosti zdieľania Word dokumentu

Po vytvorení Word dokumentu môžete pridať možnosť jeho zdieľania pomocou štandardného Intent-u pre zdieľanie:

```kotlin
fun shareWordDocument(uri: Uri) {
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    intent.putExtra(Intent.EXTRA_STREAM, uri)
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(Intent.createChooser(intent, "Zdieľať Word dokument"))
}
```

Potom môžete pridať tlačidlo pre zdieľanie dokumentu po jeho vytvorení:

```kotlin
// Tlačidlo pre zdieľanie dokumentu
Button(
    onClick = {
        val uri = exportToWord(exportFileName, exportTitle)
        if (uri != null) {
            shareWordDocument(uri)
        }
    }
) {
    Text("Zdieľať")
}
```

### Pridanie možnosti otvorenia Word dokumentu

Po vytvorení Word dokumentu môžete pridať možnosť jeho otvorenia pomocou štandardného Intent-u pre otvorenie dokumentu:

```kotlin
fun openWordDocument(uri: Uri) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.setDataAndType(uri, "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(intent)
}
```

Potom môžete pridať tlačidlo pre otvorenie dokumentu po jeho vytvorení:

```kotlin
// Tlačidlo pre otvorenie dokumentu
Button(
    onClick = {
        val uri = exportToWord(exportFileName, exportTitle)
        if (uri != null) {
            openWordDocument(uri)
        }
    }
) {
    Text("Otvoriť")
}
``` 