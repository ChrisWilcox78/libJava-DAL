/*
 * dalclient library - provides utilities to assist in using KDDart-DAL servers
 * Copyright (C) 2015  Diversity Arrays Technology
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.diversityarrays.daldb;

import java.io.IOException;
import java.util.Collection;

import com.diversityarrays.dalclient.DALClient;
import com.diversityarrays.dalclient.DalMissingParameterException;
import com.diversityarrays.dalclient.DalResponse;
import com.diversityarrays.dalclient.DalResponseException;
import com.diversityarrays.dalclient.DalUtil;

public class SampleMeasurementExportHelper {

	public static final String EXPORT_SAMPLEMEASUREMENT_CSV = "export/samplemeasurement/csv";

	public SampleMeasurementExportHelper() {
	}

	public DalResponse performExport(DALClient client,
			Collection<Integer> trialUnitIds,
			Collection<Integer> traitIds)
	throws DalResponseException, IOException, DalMissingParameterException {
		
		if (trialUnitIds==null || trialUnitIds.isEmpty()) {
			throw new DalMissingParameterException("No TrialUnitIds supplied");
		}
		if (traitIds==null || traitIds.isEmpty()) {
			throw new DalMissingParameterException("No TraitIds supplied");
		}
		
		return client.prepareQuery(EXPORT_SAMPLEMEASUREMENT_CSV)
				.setParameter("TrialUnitIdCSV", DalUtil.join(",", trialUnitIds))
				.setParameter("TraitIdCSV", DalUtil.join(",", traitIds))
				.execute();
	}
}
