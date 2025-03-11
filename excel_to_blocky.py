#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
Excel to Blocky Converter

Tento skript konvertuje Excel súbor na formát, ktorý sa dá ľahšie importovať 
do aplikácie Blocky. Skript vytvorí textový súbor s položkami vo formáte,
ktorý je možné manuálne zadať do aplikácie.

Použitie:
    python excel_to_blocky.py input.xlsx output.txt

Požiadavky:
    - Python 3.6+
    - pandas
    - openpyxl

Inštalácia požiadaviek:
    pip install pandas openpyxl
"""

import sys
import pandas as pd
import os
from datetime import datetime


def convert_excel_to_blocky(input_file, output_file):
    """
    Konvertuje Excel súbor na formát pre aplikáciu Blocky.
    
    Args:
        input_file (str): Cesta k vstupnému Excel súboru
        output_file (str): Cesta k výstupnému textovému súboru
    """
    try:
        # Načítanie Excel súboru
        print(f"Načítavam Excel súbor: {input_file}")
        df = pd.read_excel(input_file)
        
        # Kontrola, či Excel obsahuje potrebné stĺpce
        if len(df.columns) < 2:
            print("Chyba: Excel súbor musí obsahovať aspoň 2 stĺpce (popis a suma)")
            return False
        
        # Predpokladáme, že prvý stĺpec je popis a druhý je suma
        description_col = df.columns[0]
        amount_col = df.columns[1]
        
        print(f"Používam stĺpce: '{description_col}' pre popis a '{amount_col}' pre sumu")
        
        # Vytvorenie výstupného súboru
        with open(output_file, 'w', encoding='utf-8') as f:
            f.write("# Položky pre import do aplikácie Blocky\n")
            f.write("# Vytvorené: " + datetime.now().strftime("%d.%m.%Y %H:%M:%S") + "\n")
            f.write("# Zdrojový súbor: " + os.path.basename(input_file) + "\n")
            f.write("#\n")
            f.write("# Formát: Popis | Suma\n")
            f.write("# Záporné sumy sú označené znamienkom mínus (-)\n")
            f.write("#\n")
            
            # Zápis položiek
            total_amount = 0
            valid_items = 0
            invalid_items = 0
            
            for index, row in df.iterrows():
                try:
                    description = str(row[description_col]).strip()
                    
                    # Kontrola, či je suma číselná hodnota
                    try:
                        amount = float(row[amount_col])
                        total_amount += amount
                        
                        # Formátovanie sumy
                        amount_str = f"{amount:.2f}".replace('.', ',')
                        
                        # Zápis položky
                        f.write(f"{description} | {amount_str}\n")
                        valid_items += 1
                    except (ValueError, TypeError):
                        print(f"Upozornenie: Riadok {index+2} obsahuje neplatnú sumu: {row[amount_col]}")
                        invalid_items += 1
                except Exception as e:
                    print(f"Chyba pri spracovaní riadku {index+2}: {str(e)}")
                    invalid_items += 1
            
            # Zápis súhrnu
            f.write("#\n")
            f.write(f"# Celková suma: {total_amount:.2f}\n")
            f.write(f"# Počet položiek: {valid_items}\n")
            if invalid_items > 0:
                f.write(f"# Počet neplatných položiek: {invalid_items}\n")
        
        print(f"Konverzia dokončená. Výstupný súbor: {output_file}")
        print(f"Celkový počet položiek: {valid_items}")
        print(f"Celková suma: {total_amount:.2f}")
        if invalid_items > 0:
            print(f"Počet neplatných položiek: {invalid_items}")
        
        return True
    
    except Exception as e:
        print(f"Chyba pri konverzii: {str(e)}")
        return False


def print_usage():
    """Zobrazí návod na použitie"""
    print("Použitie:")
    print("  python excel_to_blocky.py input.xlsx output.txt")
    print()
    print("Parametre:")
    print("  input.xlsx  - Vstupný Excel súbor")
    print("  output.txt  - Výstupný textový súbor")


def main():
    """Hlavná funkcia"""
    # Kontrola argumentov
    if len(sys.argv) != 3:
        print_usage()
        return
    
    input_file = sys.argv[1]
    output_file = sys.argv[2]
    
    # Kontrola, či vstupný súbor existuje
    if not os.path.isfile(input_file):
        print(f"Chyba: Vstupný súbor '{input_file}' neexistuje")
        return
    
    # Kontrola, či vstupný súbor je Excel
    if not input_file.endswith(('.xlsx', '.xls')):
        print(f"Upozornenie: Vstupný súbor '{input_file}' nemusí byť Excel súbor")
    
    # Konverzia
    convert_excel_to_blocky(input_file, output_file)


if __name__ == "__main__":
    main() 