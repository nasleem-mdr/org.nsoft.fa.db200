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
