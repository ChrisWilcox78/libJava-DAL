/*
 * dalclient library - provides utilities to assist in using KDDart-DAL servers
 * Copyright (C) 2015,2016,2017 Diversity Arrays Technology
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
package com.diversityarrays.dalclient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides a means of extracting all of the "records" in a DalResponse.
 * <p>
 * Usage:
 * <pre>
 * DalResponse response = client.performQuery(...);
 * DalResponseRecords records = new DalResponseRecords(response);
 * for (String tagName : records.getResultTagNames()) {
 *     for (DalResponseRecord record : records.getRecordList(tagname)) {
 *        .. do something with <i>record</i>
 *     }
 * }
 * </pre>
 * @author brian
 */
public class DalResponseRecords {
	
	private Map<String, List<DalResponseRecord>> recordListByTagName = new HashMap<String, List<DalResponseRecord>>();

	public DalResponseRecords(DalResponse response) throws DalResponseFormatException, DalResponseException {
		DalResponseRecordVisitor visitor = new DalResponseRecordVisitor() {
			
			@Override
			public boolean visitResponseRecord(String resultTagName, DalResponseRecord data) {
				List<DalResponseRecord> recordList = recordListByTagName.get(resultTagName);
				if (recordList==null) {
					recordList = new ArrayList<DalResponseRecord>();
					recordListByTagName.put(resultTagName, recordList);
				}
				recordList.add(data);
				return true;
			}
		};
		response.visitResults(visitor);
	}
	
	/**
	 * Return the set of all the resultTagName values collected from the DalResponse.
	 * @return a Set of strings
	 */
	public Set<String> getResultTagNames() {
		return Collections.unmodifiableSet(recordListByTagName.keySet());
	}

	/**
	 * Return the list of all records collected for the given tagName.
	 * This is usually a single string but some DAL operations <i>may</i> return
	 * more. Consult the DAL documentation for further details.
	 * @param tagName
	 * @return a List of DalResponseRecord
	 */
	public List<DalResponseRecord> getRecordList(String tagName) {
		return recordListByTagName.get(tagName);
	}
}
