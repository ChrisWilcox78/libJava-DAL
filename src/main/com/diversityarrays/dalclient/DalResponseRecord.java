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
package com.diversityarrays.dalclient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * This class provides access to the data retreived from the DAL server in response to a DAL operation
 * in a structure which is independent of the ResponseType used in the request. All fields are declared
 * final and public to simplify use and to emphasise that it is a "data-only" structure. 
 * </p>
 * <p>
 * Some DAL responses have sub-records which is usually from some related tables in the
 * database schema. Often, this data is from a many-to-one or many-to-many relationship and the
 * nested data provides the most useful fields from the related entity. For example;
 * <code>list/specimengroup</code> provides <code>Specimen</code> sub-records for each
 * <code>SpecimenGroup</code> record returned in the response.
 * </p>
 * @author brian
 * @since 2.0
 */
public class DalResponseRecord {
	
	/**
	 * The URL used to create the DalResponse from which this DalResponseRecord was obtained.
	 */
	public final String requestUrl;

	/**
	 * The JSON key or XML tag.
	 */
	public final String tagName;
	
	/**
	 * The top-level values from this record.
	 */
	public final Map<String,String> rowdata = new LinkedHashMap<String,String>();
	
	/**
	 * The sub-records for this response record. 
	 */
	public final Map<String,List<Map<String,String>>> nestedData = new LinkedHashMap<String,List<Map<String,String>>>();
	
	/**
	 * If non-empty, contains information about structural issues in the response detected by the client library.
	 * These should be reported to DArT.
	 */
	public final List<String> warnings = new ArrayList<String>(0);

	public DalResponseRecord(String url, String tagName) {
		this.requestUrl = url;
		this.tagName = tagName;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName()+"[nrows="+rowdata.size()+" from "+requestUrl+"]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
    public boolean isEmpty() {
        if (rowdata.isEmpty()) {
            if (nestedData.isEmpty()) {
                return true;
            }
        }
        return false;
    }

	public void addNestedData(String tag, Map<String,String> rowdata) {
		List<Map<String, String>> list = nestedData.get(tag);
		if (list==null) {
			list = new ArrayList<Map<String,String>>();
			nestedData.put(tag, list);
		}
		list.add(rowdata);
	}

}
