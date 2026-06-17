/**********************************************************************
* This file is part of iDempiere project                              *
* http://www.idempiere.com                                            *
*                                                                     *
* Copyright (C) Nasleem - Nsoft                                       *
*                                                                     *
* This program is free software; you can redistribute it and/or       *
* modify it under the terms of the GNU General Public License         *
* as published by the Free Software Foundation; either version 2      *
* of the License, or (at your option) any later version.              *
*                                                                     *
* This program is distributed in the hope that it will be useful,     *
* but WITHOUT ANY WARRANTY; without even the implied warranty of      *
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the        *
* GNU General Public License for more details.                        *
*                                                                     *
* You should have received a copy of the GNU General Public License   *
* along with this program; if not, write to the Free Software         *
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,          *
* MA 02110-1301, USA.                                                 *
*                                                                     *
* Contributors:                                                       *
* - Nasleem - NSoft                                                   *
**********************************************************************/


package org.nsoft.fa.db200.method;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.idempiere.fa.service.api.DepreciationDTO;
import org.idempiere.fa.service.api.IDepreciationMethod;

/**
 * Double Declining Balance switching to Straight Line (DB2SL), rate 200%.
 *
 * Catatan perbaikan (lihat versi sebelumnya untuk pembanding):
 * 1. Pemilihan nilai tahunan tidak lagi sekedar max(DDB, SL). Begitu nilai SL
 *    (sisa nilai yang masih bisa disusutkan dibagi sisa tahun) >= DDB, tahun
 *    tersebut dan seterusnya WAJIB memakai SL. Inilah esensi metode "switch"
 *    pada DB2SL: SL dipilih bukan karena lebih besar, tapi karena DDB sudah
 *    tidak akan pernah menghabiskan nilai aset sampai salvage dalam sisa umur
 *    yang ada, sehingga SL mengambil alih agar NBV akhir tepat sama dengan
 *    salvage.
 * 2. NBV awal tahun dihitung dengan mengulang dari tahun ke-0 sampai tahun
 *    target (bukan dari "cost" dikurangi akumulasi manual yang rawan bias
 *    pembulatan kumulatif).
 * 3. Pembulatan bulanan dikoreksi di bulan TERAKHIR setiap tahun (bulan ke-12
 *    pada tahun tersebut, atau periode terakhir keseluruhan), supaya total
 *    12 bulan selalu tepat sama dengan nilai penyusutan tahunan yang sudah
 *    dihitung. Tanpa ini, pembagian yearlyDepr/12 bisa meninggalkan sisa
 *    pembulatan beberapa rupiah yang membuat total akumulasi akhir asset
 *    tidak pas dengan (cost - salvage).
 * 4. Limit global lama (`cost - salvage - manualAccumulated`) yang menjadi
 *    sumber bug "salvage tidak terhitung / depresiasi berhenti sebelum
 *    waktunya" telah dihapus, karena floor salvage sudah dijamin benar pada
 *    level tahunan oleh `max_yearly = nbv - salvage` pada langkah 1.
 */
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
        int lifeYearsInt = lifeYears.intValue();
        if (lifeYearsInt <= 0) return BigDecimal.ZERO;

        BigDecimal annualRate = BD_2.divide(lifeYears, 8, RoundingMode.HALF_UP);

        int targetYearIdx = (periodSequence - 1) / 12;
        boolean isLastPeriodOverall = periodSequence >= lifeMonths.intValue();

        // --- Hitung NBV awal tahun & nilai penyusutan tahun target dengan
        //     mengulang dari tahun pertama. Ini menggantikan loop manual
        //     "manualAccumulated" pada versi sebelumnya. ---
        BigDecimal nbv = cost;
        BigDecimal yearlyDeprForTargetYear = BigDecimal.ZERO;

        for (int y = 0; y <= targetYearIdx; y++) {
            int remainingYears = lifeYearsInt - y;

            BigDecimal ddbYearly = nbv.multiply(annualRate);
            BigDecimal slYearly = BigDecimal.ZERO;
            if (remainingYears > 0) {
                slYearly = nbv.subtract(salvage)
                        .divide(BigDecimal.valueOf(remainingYears), 8, RoundingMode.HALF_UP);
            }

            // *** Logika switch DB2SL yang benar: pilih SL begitu SL >= DDB ***
            BigDecimal yearDepr;
            if (slYearly.compareTo(ddbYearly) >= 0) {
                yearDepr = slYearly;
            } else {
                yearDepr = ddbYearly;
            }

            // Floor: jangan sampai menyusutkan lebih dari sisa nilai di atas salvage
            BigDecimal maxYearly = nbv.subtract(salvage);
            if (yearDepr.compareTo(maxYearly) > 0) {
                yearDepr = maxYearly;
            }
            if (yearDepr.compareTo(BigDecimal.ZERO) < 0) {
                yearDepr = BigDecimal.ZERO;
            }

            BigDecimal yearDeprRounded = yearDepr.setScale(scale, RoundingMode.HALF_UP);

            if (y == targetYearIdx) {
                yearlyDeprForTargetYear = yearDeprRounded;
            }

            nbv = nbv.subtract(yearDeprRounded);
        }

        BigDecimal monthlyDepr = yearlyDeprForTargetYear.divide(BD_12, scale, RoundingMode.HALF_UP);

        // --- Koreksi pembulatan: bulan terakhir pada tahun ini (atau periode
        //     terakhir keseluruhan) menyerap sisa pembulatan, supaya total
        //     12 bulan == yearlyDeprForTargetYear secara presisi. ---
        int monthsPassedInYear = (periodSequence - 1) % 12;
        boolean isLastMonthOfThisYear = (monthsPassedInYear == 11) || isLastPeriodOverall;

        if (isLastMonthOfThisYear) {
            BigDecimal accumulatedBeforeThisMonth = monthlyDepr.multiply(BigDecimal.valueOf(monthsPassedInYear));
            monthlyDepr = yearlyDeprForTargetYear.subtract(accumulatedBeforeThisMonth);
        }

        if (monthlyDepr.compareTo(BigDecimal.ZERO) < 0) {
            monthlyDepr = BigDecimal.ZERO;
        }

        return monthlyDepr.setScale(scale, RoundingMode.HALF_UP);
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