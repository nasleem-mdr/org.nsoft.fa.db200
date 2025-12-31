# Fixed Asset Declining Balance Depreciation Method
IDempiere Plugin for handle Declining Balance (DB2SL- 200% and DB1SL-150%)
Test with IDempiere 11

# How this plugin calculate
## Sample DB2SL : 
1. Asset Cost IDR 1.200.000
2. Salvage 0
3. UseLive 4 years
4. Calculate
5. AccumulatedCost
6. Rate 200%
   - 1st Year
      - (Asset Cost - Salvage - AccumulatedCost) x rate
      - Yearly Dep = (1.200.000-0-0)x2/4
      - Monthly Dep = 600.000/12
   - 2nd Year
      - Yearly Dep = (1.200.000-0-600)x2/4
      - Monthly Dep = 300.000/12
   - 3td Year
      - Yearly Dep = (1.200.000-0-900)x2/4
      - Montly Dep = 150.000/12
   - 4th Year 
      - Yearly Dep = 1.200.000-1.050.000
      - Montly Dep = 150.000/12
        
Year | Value     | rate | depreciation|  sum depreciacion | Net Book Value
--------------------------------------------------------------------------
2022 | 1.200.000 | 50% |    600.000   |         600.000   | 600.000
2023 |   600.000 | 50% |    300.000   |         900.000   | 300.000
2024 |   300.000 | 50% |    150.000   |       1.050.000   | 150.000
2025 |   150.000 | 100%|    150.000   |       1.200.000   |    0

   
# How to use
1. Install
2. Create or Activate Declining Balance Methode (Depreciation Method Window) - DB2SL (200% DB to SL) or DB1SL(150% DB to SL)
3. Setup Asset as Usual --> use "Declining Balance DB2SL/DB1SL".
4. Asset transaction --> Asset Addition
5. Depreciation Processing --> Post Depreciation Entry
