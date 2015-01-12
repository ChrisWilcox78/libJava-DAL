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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.collections15.Transformer;

import com.diversityarrays.dalclient.DALClient;
import com.diversityarrays.dalclient.DalMissingParameterException;
import com.diversityarrays.dalclient.DalResponse;
import com.diversityarrays.dalclient.DalResponseException;
import com.diversityarrays.dalclient.DalResponseRecord;
import com.diversityarrays.dalclient.DalResponseRecordVisitor;
import com.diversityarrays.util.Pair;

public class TrialUtils {
	/**
	 * Return a Transformer which can transform a Pair&lt;String&gt; to a user-meaningful
	 * string which can be used to select the Pair.
	 * @param rowPrefix
	 * @param rangePrefix
	 * @return a Transformer
	 */
	static public Transformer<Pair<String,String>,String> createRowRangeTransformer(final String rowPrefix, final String rangePrefix) {
		return new Transformer<Pair<String,String>,String>() {
			@Override
			public String transform(Pair<String,String> p) {
				return rowPrefix + p.a + rangePrefix + p.b;
			}
		};
	}
	
	/**
	 * Used in conjunction with <code>createRowRangeTransformer(rowPrefix, rangePrefix)</code>, this will extract the element
	 * from the Pair which corresponds to the one used for the "rowPrefix".
	 */
	static public final Transformer<Pair<String,String>,String> ROW_EXTRACTER = 
			new Transformer<Pair<String,String>,String>() {
		@Override
		public String transform(Pair<String,String> p) {
			return p.a;
		}
	};
	
	/**
	 * Used in conjunction with <code>createRowRangeTransformer(rowPrefix, rangePrefix)</code>, this will extract the element
	 * from the Pair which corresponds to the one used for the "rangePrefix".
	 */
	static public final Transformer<Pair<String,String>,String> RANGE_EXTRACTER = 
			new Transformer<Pair<String,String>,String>() {
		@Override
		public String transform(Pair<String,String> p) {
			return p.b;
		}
	};
	
	/**
	 * Return the prefixes (a.k.a. field names) used in the UnitPosition records for the specified trial.
	 * If no TrialUnits exist for the Trial then return null.
	 * Otherwise return the matches found in the list of available database unit position field names.
	 * @param client
	 * @param trialId
	 * @return null or a List of String
	 * names (i.e. prefixes) used
	 * @throws DalResponseException
	 * @throws DalMissingParameterException
	 * @throws IOException
	 */
	static public List<String> getTrialUnitPositionFieldNames(DALClient client, Integer trialId) throws DalResponseException, DalMissingParameterException, IOException {
		return getTrialUnitPositionFieldNames(client, trialId, null);
	}
	
	/**
	 * Return the prefixes (a.k.a. field names) used in the UnitPosition records for the specified trial.
	 * If no TrialUnits exist for the Trial then return null.
	 * Otherwise return the matches found in the list of available database unit position field names.
	 * @param client a DALClient that is logged-in
	 * @param trialId identifies the trial
	 * @param unitPositionFieldNames result from DAL operation "list/unitpositionfield"
	 * @return null or a List of String
	 * @throws DalResponseException
	 * @throws DalMissingParameterException
	 * @throws IOException
	 */
	static public List<String> getTrialUnitPositionFieldNames(DALClient client, Integer trialId, Collection<String> unitPositionFieldNames) throws DalResponseException, DalMissingParameterException, IOException {

		if (unitPositionFieldNames==null) {
			unitPositionFieldNames = collectDatabaseUnitPositionFieldNames(client);
		}
		
		// Getting the first UnitPosition should be all we need - we assume that
		// the Trial design has been consistent in using the one Row/Range pairing for
		// all the UnitPosition values for all TrialUnits in the Trial.
		DalResponse tuResponse = client.prepareQuery("trial/_trialid/list/trialunit/_nperpage/page/_num")
			.setParameter("_trialid", trialId)
			.setParameter("_nperpage", 1)
			.setParameter("_num", 1)
			.execute();
		
		// If there are no TrialUnits for the Trial we will get a null result...
		String unitPosition = tuResponse.getRecordFieldValue("TrialUnit", "UnitPositionText");
		
		List<String> result = null;
		if (unitPosition != null) {
			result = extractUnitPositionNames(unitPosition, unitPositionFieldNames);
		}
		return result;
	}
	
	/**
	 * Retrieve the names of the fields (prefixes) used for UnitPositions from DAL.
	 * So if UnitPosition records contain "Range1|Row27" or "Column45|Row13" this
	 * will return the set containing "Range", "Row" and "Column".
	 * @param client
	 * @return a List of String
	 * @throws DalResponseException
	 * @throws IOException
	 */
	static public List<String> collectDatabaseUnitPositionFieldNames(DALClient client) throws DalResponseException, IOException {
		final List<String> unitPositionFieldNames = new ArrayList<String>();
		DalResponse upf = client.performQuery("list/unitpositionfield");
		DalResponseRecordVisitor upfVisitor = new DalResponseRecordVisitor() {
			@Override
			public boolean visitResponseRecord(String resultTagName, DalResponseRecord record) {
				unitPositionFieldNames.add(record.rowdata.get("FieldName"));
				return true;
			}
		};
		upf.visitResults(upfVisitor, "UnitPositionField");
		
		return unitPositionFieldNames;
	}

	/**
	 * Extract and return from the trialUnitPosition the "prefixes" which occur in the supplied
	 * list of unit position field names.
	 * You can use <code>collectUnitPositionFieldNames()</code> to
	 * get the list from DAL.
	 * @param trialUnitPosition
	 * @param unitPositionFieldNames as returned by "list/unitpositionfield"
	 * @return a List of String in the order they occur in trialUnitPosition
	 */
	static public List<String> extractUnitPositionNames(String trialUnitPosition, Collection<String> unitPositionFieldNames) {
		
		List<String> result = new ArrayList<String>();
	
		for (String part : trialUnitPosition.split("\\|")) {
			
			String colName = null;
			for (String upfn : unitPositionFieldNames) {
				if (part.matches("^"+Pattern.quote(upfn)+"[0-9]+$")) {
					colName = upfn;
					break;
				}
			}

			if (colName != null && ! result.contains(colName)) {
				result.add(colName);
			}
		}
		return result;
	}
	
}
