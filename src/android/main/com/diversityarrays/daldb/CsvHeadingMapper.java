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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.diversityarrays.dalclient.DalUtil;

/**
 * A very simple CSV heading manager.
 * @author brian
 */
public class CsvHeadingMapper {

	final private List<String> lowcaseFieldNames ;
	final private List<String> fieldNames;
	final private Map<Integer,String> headingByColumnIndex = new HashMap<Integer,String>();
	
	private int maxFoundColumnIndex = 0;
	private Integer columnCount;
	
	public CsvHeadingMapper(String... fieldNames) {
		this(Arrays.asList(fieldNames));
	}
	
	public CsvHeadingMapper(List<String> fieldNames) {
		this.fieldNames = fieldNames;
		lowcaseFieldNames = new ArrayList<String>(fieldNames.size());
		for (String s : fieldNames) {
			lowcaseFieldNames.add(s.toLowerCase());
		}
	}
	
	public Map<String, String> createHeadingToColumnIndex(String inputPath, String line) throws MissingFieldException {
		
		Map<String, String> result = new LinkedHashMap<String,String>();
		
		String[] headings = DalUtil.splitCsvLine(line.toLowerCase(), ',', '"', null);
		
		for (int hi = 0; hi < headings.length; ++hi) {
			String hdg = headings[hi];
			hdg = hdg.replaceAll("^\"", "");
			hdg = hdg.replaceAll("\"$", "");
			
			headingByColumnIndex.put(hi, hdg);
			
			int pos = lowcaseFieldNames.indexOf(hdg);
			if (pos>=0) {
				result.put(fieldNames.get(pos), Integer.toString(hi));
				
				maxFoundColumnIndex = Math.max(maxFoundColumnIndex, hi);
				columnCount  = headings.length;
			}
		}

		List<String> missing = new ArrayList<String>();
		for (String fname : fieldNames) {
			if (! result.containsKey(fname)) {
				missing.add(fname);
			}
		}

		if (! missing.isEmpty()) {
			StringBuilder sb = new StringBuilder("Missing fields:");
			String sep = " ";
			for (String m : missing) {
				sb.append(sep).append(m);
				sep = ",";
			}
			throw new MissingFieldException(sb.toString(), "Line 1 of "+inputPath);
		}
		
		return result;
	}

	// 1-based
	public int getMaxFoundColumnIndex() {
		return maxFoundColumnIndex;
	}

	public Integer getColumnCount() {
		return columnCount;
	}

	// 0-based
	public String getHeading(int columnIndex) {
		return headingByColumnIndex.get(columnIndex);
	}
}