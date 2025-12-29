# org.nsoft.fa.db200
IDempiere Plugin for handle Declining Balance (DB2SL and DB1SL)
Test with IDempiere 11

#How this plugin calculate
Sample DB2SL : 
1. Asset Cost IDR 1.200.000
2. Salvage 0
3. UseLive 4 years
4. Calculate
5. AccumulatedCost
6. Rate 200%
   a. 1st Year
      (Asset Cost - Salvage - AccumulatedCost) x rate
      Yearly Dep = (1.200.000-0-0)x2/4
      Monthly Dep = 600.000/12
   b. 2nd Year
      Yearly Deo = (1.200.000-0-600)x2/4
      Monthly Dep = 300.000/12
   c


   
#How to use
1. Install
2. Create or Activate Declining Balance Methode (Depreciation Method Window) - DB2SL/DB1SL
3. Setup Asset as Usual, use "Declining Balance DB2SL".
