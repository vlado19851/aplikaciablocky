# Implementácia importu Excel súborov v aplikácii Blocky

Tento dokument popisuje, ako implementovať funkciu importu Excel súborov v budúcich verziách aplikácie Blocky.

## Potrebné závislosti

Do súboru `build.gradle` (app) pridajte nasledujúce závislosti:

```gradle
dependencies {
    // Existujúce závislosti
    
    // Apache POI pre prácu s Excel súbormi
    implementation 'org.apache.poi:poi:5.2.3'
    implementation 'org.apache.poi:poi-ooxml:5.2.3'
}
```

## Implementácia triedy ExcelImporter

Vytvorte novú triedu `ExcelImporter.kt` podľa priloženého vzoru v súbore `import_excel.kt`. Táto trieda obsahuje základnú funkcionalitu pre:

1. Otvorenie Excel súboru
2. Čítanie údajov z buniek
3. Konverziu hodnôt na správne dátové typy
4. Ukladanie údajov do priečinka aplikácie

## Integrácia do používateľského rozhrania

### 1. Pridanie tlačidla pre import

V súbore `FolderContentScreen.kt` pridajte tlačidlo pre import Excel súboru:

```kotlin
// V TopAppBar pridajte nové tlačidlo
IconButton(onClick = { selectExcelFile() }) {
    Icon(
        imageVector = Icons.Default.FileUpload, // Potrebné pridať import
        contentDescription = "Importovať Excel"
    )
}
```

### 2. Implementácia funkcie pre výber súboru

V hlavnej aktivite alebo fragmente implementujte funkciu pre výber súboru:

```kotlin
private val REQUEST_EXCEL_FILE = 1001
private val excelImporter = ExcelImporter(this)

private fun selectExcelFile() {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "application/vnd.ms-excel"
        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
            "application/vnd.ms-excel",                  // .xls
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" // .xlsx
        ))
    }
    startActivityForResult(intent, REQUEST_EXCEL_FILE)
}
```

### 3. Spracovanie výsledku výberu súboru

Implementujte metódu `onActivityResult` pre spracovanie výsledku výberu súboru:

```kotlin
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == REQUEST_EXCEL_FILE && resultCode == Activity.RESULT_OK) {
        data?.data?.let { uri ->
            // Predpokladáme, že currentFolder je aktuálny priečinok
            val importedCount = excelImporter.importExcelFile(uri, currentFolder)
            
            // Aktualizácia zoznamu súborov
            files.clear()
            currentFolder.listFiles()?.forEach { file ->
                files.add(file)
            }
            
            // Aktualizácia celkovej sumy
            calculateFolderTotalSum()
        }
    }
}
```

### 4. Aktualizácia manifestu

V súbore `AndroidManifest.xml` pridajte povolenia pre prístup k úložisku:

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

1. Vytvorte testovací Excel súbor s nasledujúcou štruktúrou:
   - Stĺpec A: Popis položky
   - Stĺpec B: Suma

2. Otestujte import s rôznymi typmi údajov:
   - Kladné a záporné čísla
   - Rôzne formáty čísel (s bodkou aj čiarkou ako desatinným oddeľovačom)
   - Prázdne bunky
   - Vzorce

## Možné vylepšenia

1. **Mapovanie stĺpcov**: Pridajte možnosť vybrať, ktorý stĺpec obsahuje popis a ktorý sumu
2. **Náhľad pred importom**: Zobrazte používateľovi náhľad údajov pred ich importom
3. **Hromadný import**: Umožnite import viacerých súborov naraz
4. **Filtrovanie**: Umožnite používateľovi filtrovať, ktoré riadky sa majú importovať
5. **Podpora pre viac hárkov**: Pridajte možnosť vybrať, z ktorého hárku sa majú údaje importovať

## Poznámky

- Knižnica Apache POI môže zvýšiť veľkosť APK. Zvážte použitie ProGuard/R8 na minimalizáciu veľkosti.
- Spracovanie veľkých Excel súborov môže byť náročné na pamäť. Implementujte spracovanie po častiach alebo v samostatnom vlákne.
- Testujte funkcionalitu na rôznych verziách Androidu a rôznych zariadeniach. 