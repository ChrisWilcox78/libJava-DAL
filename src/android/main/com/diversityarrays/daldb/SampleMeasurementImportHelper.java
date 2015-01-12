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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import com.diversityarrays.dalclient.DALClient;
import com.diversityarrays.dalclient.DalException;
import com.diversityarrays.dalclient.DalResponse;
import com.diversityarrays.dalclient.DalResponseException;

public class SampleMeasurementImportHelper {
	
	public static final String IMPORT_SAMPLEMEASUREMENT_CSV = "import/samplemeasurement/csv";

	
	public static final String[] SAMPLEMEASUREMENT_REQUIRED_HEADINGS = {
		"TrialUnitId",
		"SampleTypeId",
		"TraitId",
		"MeasureDateTime",
		"InstanceNumber",
		"TraitValue",
	};
	
	private final File dataFile;
	
	private CsvHeadingMapper csvHeadingMapper = new CsvHeadingMapper(SAMPLEMEASUREMENT_REQUIRED_HEADINGS);

	private Map<String, String> postParameters;


	public SampleMeasurementImportHelper(File data)
	throws IOException, DalException, MissingFieldException 
	{
		this.dataFile = data;
		
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(dataFile));

			String line = br.readLine();
			if (line==null) {
				throw new DalException("Data file is empty: "+dataFile.getPath());
			}

			// We need to read the first line (of headings) to figure out the columns used
			// so we can tell DAL the correct values for TraitDataStartCol and TraitDataEndCol.
			
			// NOTE: Implicit assumption is that the headings line (line#1) defines everything
			postParameters = csvHeadingMapper.createHeadingToColumnIndex(dataFile.getPath(), line);

		}
		finally {
			if (br!=null) {
				try { br.close(); }
				catch (IOException ignore) {}
			}
		}
	}
	
	public File getDataFile() {
		return dataFile;
	}
	
	public CsvHeadingMapper getCsvHeadingMapper() {
		return csvHeadingMapper;
	}
	
	public DalResponse performImport(DALClient client)
	throws DalResponseException, IOException {
		
		return client.prepareUpload(IMPORT_SAMPLEMEASUREMENT_CSV, dataFile)
				.addPostParameters(postParameters)
				.execute();
	}

}
