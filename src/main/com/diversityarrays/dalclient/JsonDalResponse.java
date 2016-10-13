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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.diversityarrays.dalclient.DalUtil.JsonResult;

/**
 * Concrete implementation of DalResponse for JSON formatted data.
 * @author brian
 *
 */
public class JsonDalResponse extends AbstractDalResponse {

	public JsonDalResponse(String url, HttpResponseInfo responseInfo) {
		super(url, responseInfo);
	}

	@Override
	public ResponseType getResponseType() {
		return ResponseType.JSON;
	}

	// GroupName
	private JsonResult savedJsonResult = null;
	
	private JsonResult getJsonResult() throws DalResponseFormatException {
		if (savedJsonResult==null) {
			String raw = getRawResponse();
			if (DalUtil.looksLikeDoctype(raw)) {
				throw new DalResponseFormatException("response is a DTD");
			}
			
			long elapsed = System.currentTimeMillis();
			savedJsonResult = DalUtil.parseJson(getUrl(), raw);

			if (SHOW_TIMING) {
				elapsed = System.currentTimeMillis() - elapsed;
				System.err.println(this.getClass().getName()+"_parseResult: url="+getUrl()); //$NON-NLS-1$
				System.err.println("\tserver ms="+getHttpResponseInfo().elapsedMillis+"\tjson parse ms="+elapsed); //$NON-NLS-1$ //$NON-NLS-2$
			}

			if (savedJsonResult==null) {
				String first50 = raw.substring(0, Math.min(raw.length(), 50));
				String ellipsis = (raw.length() > first50.length()) ? "..." : ""; //$NON-NLS-1$ //$NON-NLS-2$
				throw new DalResponseFormatException("Invalid JSON result: '"+first50+ellipsis+"'");
			}
		}
		return savedJsonResult;
	}
	
	@Override
	public DalResponseRecord getFirstRecord(String key) throws DalResponseFormatException, DalResponseException {
		JsonResult jsonResult = getJsonResult();
		String errorMessage = jsonResult.getJsonlDalErrorMessage();
		if (errorMessage!=null) {
			throw new DalResponseException(errorMessage);
		}
		
		return jsonResult.getFirstRecord(key);
	}
	
	@Override
	public boolean visitResults(DalResponseRecordVisitor visitor, Collection<String> wantedTagNames)
	throws DalResponseFormatException, DalResponseException
	{
		boolean result = true;
		
		JsonResult jsonResult = getJsonResult();
		String errorMessage = jsonResult.getJsonlDalErrorMessage();
		if (errorMessage!=null) {
			throw new DalResponseException(errorMessage);
		}
		
		List<String> tagNames = null;
		if (wantedTagNames!=null && ! wantedTagNames.isEmpty()) {
			if (wantedTagNames instanceof List) {
				tagNames = (List<String>) wantedTagNames;
			}
			else {
				tagNames = new ArrayList<String>(wantedTagNames);
			}
		}
		result = jsonResult.visitResults(visitor, tagNames, getWantEmptyRecords());
		
		return result;
	}


	@Override
	public boolean visitResults(DalResponseRecordVisitor visitor, String ... wantedTagNames)
	throws DalResponseFormatException, DalResponseException
	{
		boolean result = true;
		
		JsonResult jsonResult = getJsonResult();
		String errorMessage = jsonResult.getJsonlDalErrorMessage();
		if (errorMessage!=null) {
			throw new DalResponseException(errorMessage);
		}
		
		List<String> tagNames = null;
		if (wantedTagNames!=null && wantedTagNames.length>0) {
			tagNames = Arrays.asList(wantedTagNames);
		}
		result = jsonResult.visitResults(visitor, tagNames, getWantEmptyRecords());

		return result;
	}

	@Override
	public String getRecordFieldValue(String recordName, String fieldName) throws DalResponseFormatException, DalResponseException {
		return getJsonResult().getJsonRecordFieldValue(recordName, fieldName);
	}

	@Override
	public String getResponseErrorMessage() throws DalResponseFormatException {
		return getJsonResult().getJsonRecordFieldValue("Error", "Message"); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
