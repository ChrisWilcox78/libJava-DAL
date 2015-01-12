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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.diversityarrays.dalclient.DALClient;
import com.diversityarrays.dalclient.DalException;
import com.diversityarrays.dalclient.DalResponse;
import com.diversityarrays.dalclient.DalResponseException;

/**
 * Provide a helper class to make it easier to deal with the KDSmart template files
 * supplied by DAL.
 * @author brian
 *
 */
public class TraitDataCaptureHelper {

	private static final String TRAIT_DATA_END_COL = "TraitDataEndCol";

	private static final String TRAIT_DATA_START_COL = "TraitDataStartCol";

	private static final String SAMPLE_TYPE_VALUE = "SampleTypeValue";

	private static final String ROW_COL_NAME = "RowColName";

	private static final String COLUMN_COL_NAME = "ColumnColName";

//	public static final String IMPORT_DATAKAPTUREDATA_CSV = "import/datakapturedata/csv";
	
	public static final List<String> TRIAL_HEADINGS;
	
	public static final List<String> TRIAL_UNIT_HEADINGS;
	
	public static final List<String> UNIT_POSITION_HEADINGS;
	
	public static final List<String> ALL_REQUIRED_HEADINGS;
	
	static {
		TRIAL_HEADINGS = Collections.unmodifiableList(Arrays.asList(
				"SiteName",
				"SiteYear",
				"TrialTypeName",
				"TrialNumber",
				"TrialAcronym",
				"TrialStartDate"));

		TRIAL_UNIT_HEADINGS = Collections.unmodifiableList(Arrays.asList(
				"GenotypeName",
//				"SelectionHistory",
//				"TrialUnitComment",
				"ReplicateNumber"
				));


		UNIT_POSITION_HEADINGS = Collections.unmodifiableList(Arrays.asList(
				"Block",
				"Column",
				"Row"
				));
		
		List<String> list = new ArrayList<String>(TRIAL_HEADINGS);
		list.addAll(TRIAL_UNIT_HEADINGS);
		list.addAll(UNIT_POSITION_HEADINGS);
		list.add("Barcode"); // This marks the end of the metadata, only Traits come next
		
		ALL_REQUIRED_HEADINGS = Collections.unmodifiableList(list);
		
	}
	
	static public String[] getTrialHeadings() {
		return TRIAL_HEADINGS.toArray(new String[TRIAL_HEADINGS.size()]);
	}
	
	static public String[] getTrialUnitHeadings() {
		return TRIAL_UNIT_HEADINGS.toArray(new String[TRIAL_UNIT_HEADINGS.size()]);
	}
	
	static public String[] getUnitPositionHeadings() {
		return UNIT_POSITION_HEADINGS.toArray(new String[UNIT_POSITION_HEADINGS.size()]);
	}
	
	static public String[] getAllHeadings() {
		return ALL_REQUIRED_HEADINGS.toArray(new String[ALL_REQUIRED_HEADINGS.size()]);
	}
	
	
	private final File dataFile;
	
	private final Integer traitDataStartCol;
	private final Integer traitDataEndCol;

	private CsvHeadingMapper csvHeadingMapper = new CsvHeadingMapper(getAllHeadings());

	private Map<String, String> postParameters;

	public TraitDataCaptureHelper(File data)
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

			traitDataStartCol = 1 + csvHeadingMapper.getMaxFoundColumnIndex();
			postParameters.put(TRAIT_DATA_START_COL, traitDataStartCol.toString());

			traitDataEndCol = csvHeadingMapper.getColumnCount()-1;
			postParameters.put(TRAIT_DATA_END_COL, traitDataEndCol.toString());
			
//			int columnCount = csvHeadingMapper.getColumnCount();
//			
//			Set<String> traitValueHeadings = new HashSet<String>();
//			Set<String> traitDateHeadings = new HashSet<String>();
//			
//			Pattern pattern = Pattern.compile("Date_(.*)");
//			
//			
//			for (int c = traitDataStartCol; c <= traitDataEndCol; ++c) {
//				
//			}
			
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

	public Integer getTraitDataStartCol() {
		return traitDataStartCol;
	}

	public Integer getTraitDataEndCol() {
		return traitDataEndCol;
	}
	
	public CsvHeadingMapper getCsvHeadingMapper() {
		return csvHeadingMapper;
	}

	public Map<String, String> getPostParameters() {
		return postParameters;
	}
	
	
	public DalResponse performImport(DALClient client, Integer id,
			String columnColName, String rowColName, String sampleTypeIdValue)
	throws DalResponseException, IOException {
		
		postParameters.put(ROW_COL_NAME, rowColName);
		postParameters.put(COLUMN_COL_NAME, columnColName);
		postParameters.put(SAMPLE_TYPE_VALUE, sampleTypeIdValue);
		
		return client.prepareUpload("trial/"+id+"/import/datakapturedata/csv", dataFile)
				.addPostParameters(postParameters)
				.execute();
	}

	public DalResponse performCsvUpload(DALClient client,
			Integer id,
            String columnColName, String rowColName, String sampleTypeIdValue)
    throws DalResponseException, IOException {


		postParameters.put(ROW_COL_NAME, rowColName);
		postParameters.put(COLUMN_COL_NAME, columnColName);
		postParameters.put(SAMPLE_TYPE_VALUE, sampleTypeIdValue);

		return client.prepareUpload("trial/"+id+"/import/datakapturedata/csv", dataFile)
				.addPostParameters(postParameters)
				.execute();
	}
		
	// multimedia = tableName
    static public DalResponse performMultiMediaUpload(DALClient client, String tableName, Integer id, 
    		int typeValue, File dataFile)
    throws DalResponseException,IOException,MissingFieldException {
    	
    	Map<String, String> parameters = new HashMap<String, String>();
    	parameters.put("FileType",String.valueOf(typeValue));

        return client.prepareUpload(tableName+"/"+id+"/add/multimedia", dataFile)
        	
                .addPostParameters(parameters)
                .execute();
    }
}
