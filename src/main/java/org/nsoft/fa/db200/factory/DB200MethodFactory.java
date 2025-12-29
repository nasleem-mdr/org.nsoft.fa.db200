package org.nsoft.fa.db200.factory;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.idempiere.fa.service.api.DepreciationFactoryLookupDTO;
import org.idempiere.fa.service.api.IDepreciationMethod;
import org.idempiere.fa.service.api.IDepreciationMethodFactory;
import org.nsoft.fa.db200.method.DDBScheme200;
import org.nsoft.fa.db200.method.DDBScheme150;

public class DB200MethodFactory implements IDepreciationMethodFactory {

	 // Registrasi tipe ke dalam Map (Polymorphism Strategy)
    private static final Map<String, Supplier<IDepreciationMethod>> methods = new HashMap<>();

    static {
        methods.put("DB2SL", DDBScheme200::new);
        methods.put("DB1SL", DDBScheme150::new);
    }

    public IDepreciationMethod getDepreciationMethod(DepreciationFactoryLookupDTO lookupDto) {
        Supplier<IDepreciationMethod> supplier = methods.get(lookupDto.depreciationType);
        
        if (supplier != null) {
            return supplier.get();
        }
        
        // SANGAT PENTING: Kembalikan null agar iDempiere mencari di factory standar
        return null; 
    }
}
