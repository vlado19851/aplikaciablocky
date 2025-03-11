# Implementácia importu Word dokumentov v aplikácii Blocky

Tento dokument popisuje, ako implementovať funkciu importu Word dokumentov (.docx) v budúcich verziách aplikácie Blocky.

## Potrebné závislosti

Do súboru `build.gradle` (app) pridajte nasledujúce závislosti:

```gradle
dependencies {
    // Existujúce závislosti
    
    // Apache POI pre prácu s Word dokumentmi
    implementation 'org.apache.poi:poi:5.2.3'
    implementation 'org.apache.poi:poi-ooxml:5.2.3'
    
    // Alternatívne môžete použiť knižnicu docx4j
    // implementation 'org.docx4j:docx4j:6.1.2'
}
```

## Implementácia triedy WordImporter

Vytvorte novú triedu `WordImporter.kt` podľa nasledujúceho vzoru:

```kotlin
package com.example.aplikaciablocky

import android.content.Context
import android.net.Uri
import android.widget.Toast
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

/**
 * Trieda pre import údajov z Word dokumentu do aplikácie Blocky
 */
class WordImporter(private val context: Context) {

    /**
     * Importuje údaje z Word dokumentu do zadaného priečinka
     * @param uri URI Word dokumentu na import
     * @param targetFolder Cieľový priečinok, do ktorého sa majú údaje importovať
     * @return Počet úspešne importovaných položiek
     */
    fun importWordFile(uri: Uri, targetFolder: File): Int {
        var importedItemsCount = 0
        
        try {
            // Otvorenie súboru
            val inputStream: InputStream = context.contentResolver.openInputStream(uri) ?: return 0
            
            // Vytvorenie dokumentu
            val document = XWPFDocument(inputStream)
            
            // Zoznam položiek na import
            val items = mutableListOf<Pair<String, Double>>()
            
            // Pokus o extrakciu údajov z tabuliek
            if (document.tables.isNotEmpty()) {
                for (table in document.tables) {
                    // Predpokladáme, že prvý stĺpec je popis a druhý je suma
                    for (i in 1 until table.rows.size) { // Preskočenie hlavičky
                        val row = table.rows[i]
                        if (row.tableCells.size >= 2) {
                            val description = row.tableCells[0].text.trim()
                            val amountText = row.tableCells[1].text.trim()
                            
                            // Extrakcia čísla zo sumy
                            val amount = extractAmount(amountText)
                            
                            if (amount != null && description.isNotBlank()) {
                                items.add(Pair(description, amount))
                            }
                        }
                    }
                }
            }
            
            // Ak neboli nájdené žiadne tabuľky alebo údaje v tabuľkách,
            // pokúsime sa extrahovať údaje z textu
            if (items.isEmpty()) {
                // Hľadanie vzoru "popis: suma" alebo "popis suma €"
                val pattern = Pattern.compile("([^:]+):\\s*([\\-]?\\d+[.,]?\\d*)\\s*€?")
                
                for (paragraph in document.paragraphs) {
                    val text = paragraph.text.trim()
                    if (text.isNotBlank()) {
                        val matcher = pattern.matcher(text)
                        while (matcher.find()) {
                            val description = matcher.group(1).trim()
                            val amountText = matcher.group(2)
                            
                            val amount = extractAmount(amountText)
                            
                            if (amount != null && description.isNotBlank()) {
                                items.add(Pair(description, amount))
                            }
                        }
                    }
                }
            }
            
            // Import položiek do priečinka
            for ((description, amount) in items) {
                saveAmountToFolder(targetFolder, amount, description)
                importedItemsCount++
            }
            
            // Zatvorenie súborov
            document.close()
            inputStream.close()
            
            // Zobrazenie hlásenia o úspešnom importe
            Toast.makeText(
                context,
                "Úspešne importovaných $importedItemsCount položiek",
                Toast.LENGTH_LONG
            ).show()
            
        } catch (e: Exception) {
            // Zobrazenie chybového hlásenia
            Toast.makeText(
                context,
                "Chyba pri importe: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
        
        return importedItemsCount
    }
    
    /**
     * Extrahuje číselnú hodnotu zo zadaného textu
     * @param text Text obsahujúci číselnú hodnotu
     * @return Extrahovaná číselná hodnota alebo null, ak sa nepodarilo extrahovať
     */
    private fun extractAmount(text: String): Double? {
        return try {
            // Odstránenie nečíselných znakov okrem mínus, bodky a čiarky
            val cleanedText = text.replace(Regex("[^\\d\\-.,]"), "")
                .replace(",", ".")
            
            // Konverzia na číslo
            cleanedText.toDouble()
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Uloží sumu do priečinka
     * @param folder Priečinok, do ktorého sa má suma uložiť
     * @param amount Suma
     * @param description Popis sumy
     */
    private fun saveAmountToFolder(folder: File, amount: Double, description: String) {
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
            val currentDate = dateFormat.format(Date())
            val fileName = if (description.isNotBlank()) {
                "${currentDate}_${description.replace(" ", "_")}_${amount}€.txt"
            } else {
                "${currentDate}_imported_${amount}€.txt"
            }
            
            val file = File(folder, fileName)
            file.writeText("Suma: $amount €\nPopis: $description\nDátum: ${SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(Date())}\nImportované z Word dokumentu")
            
        } catch (e: Exception) {
            // Chyba pri ukladaní - zalogujeme ju, ale pokračujeme v importe
            e.printStackTrace()
        }
    }
}
```

## Integrácia do používateľského rozhrania

### 1. Pridanie tlačidla pre import

V súbore `FolderContentScreen.kt` pridajte tlačidlo pre import Word dokumentu:

```kotlin
// V TopAppBar pridajte nové tlačidlo
IconButton(onClick = { selectWordFile() }) {
    Icon(
        imageVector = Icons.Default.FileUpload, // Potrebné pridať import
        contentDescription = "Importovať Word"
    )
}
```

### 2. Implementácia funkcie pre výber súboru

V hlavnej aktivite alebo fragmente implementujte funkciu pre výber súboru:

```kotlin
private val REQUEST_WORD_FILE = 1002
private val wordImporter = WordImporter(this)

private fun selectWordFile() {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    }
    startActivityForResult(intent, REQUEST_WORD_FILE)
}
```

### 3. Spracovanie výsledku výberu súboru

Rozšírte metódu `onActivityResult` pre spracovanie výsledku výberu súboru:

```kotlin
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    
    if (resultCode == Activity.RESULT_OK) {
        when (requestCode) {
            REQUEST_EXCEL_FILE -> {
                data?.data?.let { uri ->
                    val importedCount = excelImporter.importExcelFile(uri, currentFolder)
                    updateFilesAndSum()
                }
            }
            REQUEST_WORD_FILE -> {
                data?.data?.let { uri ->
                    val importedCount = wordImporter.importWordFile(uri, currentFolder)
                    updateFilesAndSum()
                }
            }
        }
    }
}

private fun updateFilesAndSum() {
    // Aktualizácia zoznamu súborov
    files.clear()
    currentFolder.listFiles()?.forEach { file ->
        files.add(file)
    }
    
    // Aktualizácia celkovej sumy
    calculateFolderTotalSum()
}
```

### 4. Aktualizácia manifestu

V súbore `AndroidManifest.xml` pridajte povolenia pre prístup k úložisku (ak ešte nie sú pridané):

```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

Pre Android 10+ (API 29+) pridajte aj:

```xml
<application
    ...
    android:requestLegacyExternalStorage="true">
    ...
</application>
```

## Testovanie

1. Vytvorte testovací Word dokument s nasledujúcou štruktúrou:
   - Tabuľka s dvoma stĺpcami: Popis položky a Suma
   - Text vo formáte "popis: suma"

2. Otestujte import s rôznymi typmi údajov:
   - Kladné a záporné čísla
   - Rôzne formáty čísel (s bodkou aj čiarkou ako desatinným oddeľovačom)
   - Prázdne bunky
   - Rôzne formáty textu (tučné, kurzíva, podčiarknuté)

## Možné vylepšenia

1. **Mapovanie stĺpcov**: Pridajte možnosť vybrať, ktorý stĺpec obsahuje popis a ktorý sumu
2. **Náhľad pred importom**: Zobrazte používateľovi náhľad údajov pred ich importom
3. **Podpora pre viac formátov**: Pridajte podporu pre staršie formáty Word dokumentov (.doc)
4. **Filtrovanie**: Umožnite používateľovi filtrovať, ktoré riadky sa majú importovať
5. **Rozpoznávanie štruktúry**: Vylepšite algoritmus na rozpoznávanie štruktúry dokumentu

## Poznámky

- Knižnica Apache POI môže zvýšiť veľkosť APK. Zvážte použitie ProGuard/R8 na minimalizáciu veľkosti.
- Spracovanie veľkých Word dokumentov môže byť náročné na pamäť. Implementujte spracovanie v samostatnom vlákne.
- Testujte funkcionalitu na rôznych verziách Androidu a rôznych zariadeniach.
- Zvážte implementáciu jednotného rozhrania pre import rôznych typov dokumentov (Excel, Word, PDF, atď.). 