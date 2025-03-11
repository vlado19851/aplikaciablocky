# Návod na implementáciu tlačidla "Pridať sumu ručne" a ďalších funkcií

Tento návod vám pomôže implementovať tlačidlo "Pridať sumu ručne" a ďalšie potrebné funkcie do aplikácie Blocky, aby ste mohli manuálne pridávať položky a vytvárať tabuľky vo formáte podobnom ako vo Word dokumente.

## Kroky implementácie

### 1. Otvorte súbor FolderContentScreen.kt

Tento súbor obsahuje kód pre obrazovku s obsahom priečinka v aplikácii Blocky.

### 2. Pridajte potrebné importy

Na začiatok súboru pridajte nasledujúce importy (ak ešte nie sú pridané):

```kotlin
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
```

### 3. Pridajte premenné pre dialóg

Do funkcie `FolderContentScreen` pridajte nasledujúce premenné:

```kotlin
var showAddManualDialog by remember { mutableStateOf(false) }
var manualAmount by remember { mutableStateOf("") }
var manualDescription by remember { mutableStateOf("") }
var isNegativeAmount by remember { mutableStateOf(false) }
```

### 4. Pridajte tlačidlo "Pridať sumu ručne"

Do sekcie `floatingActionButton` v komponente `Scaffold` pridajte nasledujúci kód:

```kotlin
FloatingActionButton(
    onClick = { showAddManualDialog = true },
    modifier = Modifier.padding(16.dp)
) {
    Icon(
        imageVector = Icons.Default.Add,
        contentDescription = "Pridať sumu ručne"
    )
}
```

Ak už máte existujúce tlačidlo FloatingActionButton, môžete použiť Column na zobrazenie viacerých tlačidiel:

```kotlin
Column {
    // Existujúce tlačidlo (napr. pre skenovanie QR kódu)
    SmallFloatingActionButton(
        onClick = onScanQRCode,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Skenovať QR kód"
        )
    }
    
    // Nové tlačidlo pre ručné pridanie sumy
    FloatingActionButton(
        onClick = { showAddManualDialog = true }
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Pridať sumu ručne"
        )
    }
}
```

### 5. Pridajte dialóg pre manuálne pridanie sumy

Na koniec funkcie `FolderContentScreen` (pred uzatváraciu zátvorku) pridajte nasledujúci kód:

```kotlin
// Dialóg pre manuálne pridanie sumy
if (showAddManualDialog) {
    AlertDialog(
        onDismissRequest = { showAddManualDialog = false },
        title = { Text("Pridať sumu ručne") },
        text = {
            Column {
                TextField(
                    value = manualAmount,
                    onValueChange = { manualAmount = it },
                    label = { Text("Suma (€)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                
                TextField(
                    value = manualDescription,
                    onValueChange = { manualDescription = it },
                    label = { Text("Popis") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isNegativeAmount,
                        onCheckedChange = { isNegativeAmount = it }
                    )
                    Text("Záporná suma (výdavok)")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (manualAmount.isNotBlank()) {
                        try {
                            var amount = manualAmount.replace(",", ".").toDouble()
                            if (isNegativeAmount) {
                                amount = -amount
                            }
                            saveManualAmount(folder, amount, manualDescription, context)
                            manualAmount = ""
                            manualDescription = ""
                            isNegativeAmount = false
                            showAddManualDialog = false
                            
                            // Aktualizácia zoznamu súborov
                            files.clear()
                            folder.listFiles()?.forEach { file ->
                                files.add(file)
                            }
                            
                            // Aktualizácia celkovej sumy
                            calculateFolderTotalSum()
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Neplatná suma. Zadajte číslo.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            ) {
                Text("Pridať")
            }
        },
        dismissButton = {
            Button(
                onClick = { showAddManualDialog = false }
            ) {
                Text("Zrušiť")
            }
        }
    )
}
```

### 6. Pridajte funkciu pre uloženie ručne zadanej sumy

Mimo funkcie `FolderContentScreen` (ale stále v rámci súboru) pridajte nasledujúcu funkciu:

```kotlin
// Funkcia pre uloženie ručne zadanej sumy
private fun saveManualAmount(folder: File, amount: Double, description: String, context: Context) {
    try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        val fileName = if (description.isNotBlank()) {
            "${currentDate}_${description.replace(" ", "_")}_${amount}€.txt"
        } else {
            "${currentDate}_manual_${amount}€.txt"
        }
        
        val file = File(folder, fileName)
        file.writeText("Suma: $amount €\nPopis: $description\nDátum: ${SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(Date())}")
        
        Toast.makeText(
            context,
            "Suma bola úspešne pridaná",
            Toast.LENGTH_SHORT
        ).show()
    } catch (e: Exception) {
        Toast.makeText(
            context,
            "Chyba pri ukladaní sumy: ${e.message}",
            Toast.LENGTH_SHORT
        ).show()
    }
}
```

### 7. Pridajte funkciu pre výpočet celkovej sumy v priečinku

Ak ešte nemáte funkciu pre výpočet celkovej sumy, pridajte nasledujúcu funkciu:

```kotlin
// Funkcia pre výpočet celkovej sumy v priečinku
private fun calculateFolderTotalSum() {
    folderTotalSum = 0.0
    files.forEach { file ->
        if (!file.isDirectory && file.name.contains("€")) {
            val sumRegex = "([\\-]?\\d+[.,]\\d+)€".toRegex()
            val matchResult = sumRegex.find(file.name)
            matchResult?.groupValues?.get(1)?.let { sum ->
                folderTotalSum += sum.replace(",", ".").toDouble()
            }
        }
    }
}
```

### 8. Upravte funkciu exportToTable pre lepšie formátovanie tabuľky

Nahraďte existujúcu funkciu `exportToTable` nasledujúcim kódom:

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

## Testovanie implementácie

Po implementácii všetkých zmien by ste mali mať funkčné tlačidlo "Pridať sumu ručne" a možnosť exportovať tabuľku vo formáte podobnom ako vo Word dokumente.

### Testovanie tlačidla "Pridať sumu ručne"

1. Spustite aplikáciu Blocky
2. Otvorte existujúci priečinok alebo vytvorte nový
3. Kliknite na tlačidlo "+" (Pridať sumu ručne) v pravom dolnom rohu obrazovky
4. V dialógovom okne zadajte sumu a popis
5. Ak ide o výdavok, zaškrtnite políčko "Záporná suma (výdavok)"
6. Kliknite na "Pridať"
7. Skontrolujte, či sa položka pridala do zoznamu a či sa aktualizovala celková suma

### Testovanie exportu tabuľky

1. Po pridaní niekoľkých položiek kliknite na tlačidlo "Export"
2. Vyberte formát "Tabuľka"
3. Zadajte názov súboru a kliknite na "Exportovať"
4. Otvorte vytvorený súbor a skontrolujte, či tabuľka vyzerá podobne ako vo Word dokumente

## Riešenie problémov

### Tlačidlo "Pridať sumu ručne" sa nezobrazuje

- Skontrolujte, či ste správne implementovali kód pre tlačidlo v sekcii `floatingActionButton`
- Uistite sa, že používate správne importy pre `Icons.Default.Add`

### Dialóg sa nezobrazuje po kliknutí na tlačidlo

- Skontrolujte, či ste správne implementovali kód pre dialóg
- Uistite sa, že premenná `showAddManualDialog` je správne inicializovaná a aktualizovaná

### Chyba pri pridávaní sumy

- Skontrolujte, či používate správny formát čísla (bodka alebo čiarka ako desatinný oddeľovač)
- Uistite sa, že máte povolenia na zápis do priečinka

### Nesprávne formátovanie tabuľky

- Skontrolujte, či ste správne implementovali funkciu `exportToTable`
- Uistite sa, že používate správne formátovanie reťazcov a zarovnanie textu 