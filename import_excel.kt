package com.example.aplikaciablocky

import android.content.Context
import android.net.Uri
import android.widget.Toast
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Trieda pre import údajov z Excel súboru do aplikácie Blocky
 * Táto trieda je ukážkou, ako by mohla vyzerať implementácia importu Excel súborov
 * v budúcich verziách aplikácie.
 */
class ExcelImporter(private val context: Context) {

    /**
     * Importuje údaje z Excel súboru do zadaného priečinka
     * @param uri URI Excel súboru na import
     * @param targetFolder Cieľový priečinok, do ktorého sa majú údaje importovať
     * @return Počet úspešne importovaných položiek
     */
    fun importExcelFile(uri: Uri, targetFolder: File): Int {
        var importedItemsCount = 0
        
        try {
            // Otvorenie súboru
            val inputStream: InputStream = context.contentResolver.openInputStream(uri) ?: return 0
            
            // Vytvorenie workbook objektu
            val workbook = WorkbookFactory.create(inputStream)
            
            // Získanie prvého hárku
            val sheet = workbook.getSheetAt(0)
            
            // Iterácia cez riadky
            for (rowIndex in 1..sheet.lastRowNum) { // Začíname od 1, aby sme preskočili hlavičku
                val row = sheet.getRow(rowIndex) ?: continue
                
                // Získanie údajov z buniek
                val descriptionCell = row.getCell(0) // Predpokladáme, že popis je v prvom stĺpci
                val amountCell = row.getCell(1) // Predpokladáme, že suma je v druhom stĺpci
                
                if (descriptionCell != null && amountCell != null) {
                    val description = getCellValueAsString(descriptionCell)
                    val amount = getCellValueAsDouble(amountCell)
                    
                    if (amount != null) {
                        // Uloženie položky do priečinka
                        saveAmountToFolder(targetFolder, amount, description)
                        importedItemsCount++
                    }
                }
            }
            
            // Zatvorenie súborov
            workbook.close()
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
     * Získa hodnotu bunky ako reťazec
     * @param cell Bunka, z ktorej sa má získať hodnota
     * @return Hodnota bunky ako reťazec
     */
    private fun getCellValueAsString(cell: Cell): String {
        return when (cell.cellType) {
            CellType.STRING -> cell.stringCellValue
            CellType.NUMERIC -> cell.numericCellValue.toString()
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            CellType.FORMULA -> {
                try {
                    cell.stringCellValue
                } catch (e: Exception) {
                    try {
                        cell.numericCellValue.toString()
                    } catch (e: Exception) {
                        ""
                    }
                }
            }
            else -> ""
        }
    }
    
    /**
     * Získa hodnotu bunky ako číslo
     * @param cell Bunka, z ktorej sa má získať hodnota
     * @return Hodnota bunky ako číslo alebo null, ak bunka neobsahuje číslo
     */
    private fun getCellValueAsDouble(cell: Cell): Double? {
        return when (cell.cellType) {
            CellType.NUMERIC -> cell.numericCellValue
            CellType.STRING -> {
                try {
                    cell.stringCellValue.replace(",", ".").toDouble()
                } catch (e: Exception) {
                    null
                }
            }
            CellType.FORMULA -> {
                try {
                    cell.numericCellValue
                } catch (e: Exception) {
                    try {
                        cell.stringCellValue.replace(",", ".").toDouble()
                    } catch (e: Exception) {
                        null
                    }
                }
            }
            else -> null
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
            file.writeText("Suma: $amount €\nPopis: $description\nDátum: ${SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(Date())}\nImportované z Excel súboru")
            
        } catch (e: Exception) {
            // Chyba pri ukladaní - zalogujeme ju, ale pokračujeme v importe
            e.printStackTrace()
        }
    }
    
    /**
     * Ukážka použitia v aktivite:
     * 
     * // V aktivite alebo fragmente
     * private val excelImporter = ExcelImporter(this)
     * 
     * // Funkcia na spustenie výberu súboru
     * private fun selectExcelFile() {
     *     val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
     *         addCategory(Intent.CATEGORY_OPENABLE)
     *         type = "application/vnd.ms-excel"
     *         putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
     *             "application/vnd.ms-excel",                  // .xls
     *             "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" // .xlsx
     *         ))
     *     }
     *     startActivityForResult(intent, REQUEST_EXCEL_FILE)
     * }
     * 
     * // Spracovanie výsledku výberu súboru
     * override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
     *     super.onActivityResult(requestCode, resultCode, data)
     *     if (requestCode == REQUEST_EXCEL_FILE && resultCode == Activity.RESULT_OK) {
     *         data?.data?.let { uri ->
     *             // Predpokladáme, že currentFolder je aktuálny priečinok
     *             val importedCount = excelImporter.importExcelFile(uri, currentFolder)
     *             // Aktualizácia UI po importe
     *             updateUI()
     *         }
     *     }
     * }
     */
} 