package org.nsoft.fa.db200.method;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.idempiere.fa.service.api.DepreciationDTO;
import org.idempiere.fa.service.api.IDepreciationMethod;

public class DDBScheme200 implements IDepreciationMethod {
    private static final BigDecimal BD_12 = BigDecimal.valueOf(12);
    private static final BigDecimal BD_2 = BigDecimal.valueOf(2.0);

    @Override
    public BigDecimal caclulateDepreciation(DepreciationDTO dto) {
        BigDecimal cost = dto.totalAmount;
        BigDecimal salvage = dto.salvage;
        BigDecimal lifeMonths = dto.useFullLife;
        int periodSequence = dto.period; // 1-based index (1, 2, 3...)
        int scale = dto.scale;

        if (lifeMonths.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;

        BigDecimal lifeYears = lifeMonths.divide(BD_12, 0, RoundingMode.HALF_UP);
        BigDecimal annualRate = BD_2.divide(lifeYears, 8, RoundingMode.HALF_UP);

        // Kita hitung total penyusutan dari bulan 1 sampai (periodSequence - 1)
        // Kita tidak mengambil data dari MDepreciationWorkfile agar bisa generate dignakan
        // pada "Depreciation Expense Entry"
        BigDecimal manualAccumulated = BigDecimal.ZERO;
        BigDecimal currentNBVForLoop = cost;
        
        int currentYearIdx = (periodSequence - 1) / 12;

        // Hitung akumulasi tahun-tahun sebelumnya
        for (int y = 0; y < currentYearIdx; y++) {
            BigDecimal ddbYearly = currentNBVForLoop.multiply(annualRate);
            
            // Hitung SL sebagai pembanding untuk tahun tersebut
            int remainingY = lifeYears.intValue() - y;
            BigDecimal slYearly = currentNBVForLoop.subtract(salvage)
                                    .divide(BigDecimal.valueOf(remainingY), 8, RoundingMode.HALF_UP);
            
            BigDecimal yearDepr = ddbYearly.max(slYearly);
            
            // Jika yearDepr melebihi sisa yang bisa disusutkan
            BigDecimal maxYearly = currentNBVForLoop.subtract(salvage);
            if (yearDepr.compareTo(maxYearly) > 0) yearDepr = maxYearly;
            
            manualAccumulated = manualAccumulated.add(yearDepr.setScale(scale, RoundingMode.HALF_UP));
            currentNBVForLoop = cost.subtract(manualAccumulated);
        }

        // Sekarang kita punya NBV di awal tahun berjalan
        BigDecimal nbvStartYear = currentNBVForLoop;
        
        // Hitung nilai penyusutan tahun berjalan
        BigDecimal ddbThisYear = nbvStartYear.multiply(annualRate);
        int remainingYears = lifeYears.intValue() - currentYearIdx;
        BigDecimal slThisYear = BigDecimal.ZERO;
        if (remainingYears > 0) {
            slThisYear = nbvStartYear.subtract(salvage)
                            .divide(BigDecimal.valueOf(remainingYears), 8, RoundingMode.HALF_UP);
        }
        
        BigDecimal finalYearlyDepr = ddbThisYear.max(slThisYear);
        BigDecimal monthlyDepr = finalYearlyDepr.divide(BD_12, scale, RoundingMode.HALF_UP);

        // Tambahkan akumulasi bulan-bulan yang sudah lewat di tahun berjalan ini
        int monthsPassedInYear = (periodSequence - 1) % 12;
        manualAccumulated = manualAccumulated.add(monthlyDepr.multiply(BigDecimal.valueOf(monthsPassedInYear)));

        // --- FINAL CHECK: PERIODE TERAKHIR ATAU OVER-DEPRECIATION ---
        BigDecimal totalDepreciableLimit = cost.subtract(salvage);
        BigDecimal remainingToDepreciate = totalDepreciableLimit.subtract(manualAccumulated);

        // Jika periode terakhir, ambil sisa seluruhnya
        if (periodSequence >= lifeMonths.intValue()) {
            return remainingToDepreciate.max(BigDecimal.ZERO).setScale(scale, RoundingMode.HALF_UP);
        }

        // Pastikan penyusutan bulan ini tidak melebihi sisa limit
        if (monthlyDepr.compareTo(remainingToDepreciate) > 0) {
            monthlyDepr = remainingToDepreciate;
        }

        return monthlyDepr.max(BigDecimal.ZERO).setScale(scale, RoundingMode.HALF_UP);
    }

    @Override
    public long getCountPeriod(DepreciationDTO dto) {
        return dto.useFullLife.longValue();
    }

    @Override
    public boolean isPeriodAdjustment() {
        return true;
    }
}
