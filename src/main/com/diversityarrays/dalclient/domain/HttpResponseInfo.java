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
package com.diversityarrays.dalclient.domain;

import com.diversityarrays.dalclient.http.DalHeader;

/**
 * This class is used to hold the response information retrieved from the DAL server.
 * @author brian
 */
public class HttpResponseInfo {
	/**
	 * The HTTP Header values sent by the server.
	 */
	public DalHeader[] headers;
	/**
	 * The HTTP status code from the server response.
	 */
	public int httpStatusCode;
	/**
	 * If httpStatusCode is &lt; 200 or &gt;= 300, this will contain the error reason
	 * in the server's response.
	 */
	public String httpErrorReason; // non-null if httpStatusCode outside of [200, 300)
	/**
	 * The raw text of the server response.
	 */
	public String serverResponse; // raw server response
	/**
	 * The number of milliseconds it took for the server to respond.
	 */
	public long elapsedMillis; // number of milliseconds the request took
}
