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

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collection;

/**
 * <p>
 * Most of the DALClient methods return an instance of DalResponse
 * which holds the data returned by the DAL server as well as the
 * URL used for the query.
 * </p>
 * <p>
 * The accessor methods insulate the user from needing to know the structure of
 * the server response (whether it be XML or JSON).
 * </p>
 * @author brian
 *
 */
public interface DalResponse {

	/**
	 * Get the information contained in the HTTP response received from the DAL server.
	 * @return an instance of HttpResponseInfo
	 */
	HttpResponseInfo getHttpResponseInfo();
	
	/**
	 * Get the url which was used to obtain this DalResponse.
	 * @return the url as a String
	 */
	String getUrl();
	
	/**
	 * Get the raw text of the response from the DAL server.
	 * @return the text as a String
	 */
	String getRawResponse();
	
	/**
	 * Get the type of response which was requested from the DAL server.
	 * @return a ResponseType
	 */
	ResponseType getResponseType();
	
	/**
	 * Get the error message from the response or null if there was no error.
	 * Note that implementations are expected to extract the DAL server's custom
	 * error and/or the HTTP error reason.
	 * @return the error message as a String or null
	 * @throws DalResponseFormatException
	 */
	String getResponseErrorMessage() throws DalResponseFormatException;

	void setWantEmptyRecords(boolean b);
	boolean getWantEmptyRecords();
	
	/**
	 * Invoke the visitor for each "record" in the response data while the visitor
	 * returns true. The <i>tagname</i> of the record is provided
	 * in the call to the visitor.
	 * @param visitor
	 * @param wantedTagNames is the specific tagnames to visit
	 * @return true unless the visitor ever returns false
	 * @throws DalResponseFormatException
	 * @throws DalResponseException
	 */
	boolean visitResults(DalResponseRecordVisitor visitor, Collection<String> wantedTagNames) throws DalResponseFormatException, DalResponseException;

	/**
	 * Invoke the visitor for each "record" in the response data while the visitor
	 * returns true. The <i>tagname</i> of the record is provided
	 * in the call to the visitor.
	 * @param visitor
	 * @param wantedTagNames is the specific tagnames to visit
	 * @return true unless the visitor ever returns false
	 * @throws DalResponseFormatException
	 * @throws DalResponseException
	 */
	boolean visitResults(DalResponseRecordVisitor visitor, String ... wantedTagNames) throws DalResponseFormatException, DalResponseException;

	/**
	 * Return the first record with the specified recordName.
	 * @param recordName
	 * @return a Map of the fieldName/value data for the record
	 * @throws DalResponseFormatException
	 * @throws DalResponseException
	 */
	DalResponseRecord getFirstRecord(String recordName) throws DalResponseFormatException, DalResponseException;
	
	/**
	 * Return the value of the named field from the first record of the
	 * required recordName.
	 * @param recordName
	 * @param fieldName
	 * @return the value or null if there is no such record or field
	 * @throws DalResponseFormatException
	 * @throws DalResponseException
	 */
	String getRecordFieldValue(String recordName, String fieldName) throws DalResponseFormatException, DalResponseException;
	
	/**
	 * Print the response on the supplied output.
	 * @param output
	 */
	void printOn(PrintStream output);
	
	/**
	 * Print the response on the supplied output.
	 * @param output
	 */
	void printOn(PrintWriter output);

	/**
	 * Check if the response looks like a DTD.
	 * @return true if it does
	 */
	boolean getResponseIsDTD();

}
