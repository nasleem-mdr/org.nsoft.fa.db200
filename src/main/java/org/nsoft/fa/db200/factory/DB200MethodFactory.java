package org.nsoft.fa.db200.factory;

import org.idempiere.fa.service.api.DepreciationFactoryLookupDTO;
import org.idempiere.fa.service.api.IDepreciationMethod;
import org.idempiere.fa.service.api.IDepreciationMethodFactory;
import org.nsoft.fa.db200.method.DDBScheme200;

public class DB200MethodFactory implements IDepreciationMethodFactory {

    // Konstanta untuk nama metode yang akan digunakan di database/Application Dictionary
    public static final String DEPRECIATION_TYPE_DDB200 = "DB200";

    @Override
    public IDepreciationMethod getDepreciationMethod(DepreciationFactoryLookupDTO lookupDto) {
    	
    	if (DEPRECIATION_TYPE_DDB200.equals(lookupDto.depreciationType)) {
            // Jika sistem meminta DB200, kembalikan instance kelas perhitungan kita
            return new DDBScheme200();
        }
        return null; // Jika bukan, kembalikan null
    }
}
