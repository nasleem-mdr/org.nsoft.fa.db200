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
