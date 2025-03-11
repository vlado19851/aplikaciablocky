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

/**
 * Funkcia pre export údajov do Word dokumentu, ktorú je potrebné pridať do triedy FolderContentScreen
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

/**
 * Kód pre pridanie tlačidla na export do Word dokumentu do dialógu pre export
 * Tento kód je potrebné pridať do existujúceho dialógu pre export v FolderContentScreen.kt
 */
/*
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
*/

/**
 * Potrebné závislosti, ktoré je potrebné pridať do build.gradle (app)
 */
/*
dependencies {
    // Existujúce závislosti
    
    // Apache POI pre prácu s Word dokumentmi
    implementation 'org.apache.poi:poi:5.2.3'
    implementation 'org.apache.poi:poi-ooxml:5.2.3'
}
*/ 