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
package com.diversityarrays.dalclient.exception;

import com.diversityarrays.dalclient.domain.HttpResponseInfo;

/**
 * This DalResponseException subclass is only thrown when the response
 * problem is caused by an HTTP error code. The message includes both
 * the HTTP error reason as well as and DAL "Error" information.
 * @author brian
 *
 */
public class DalResponseHttpException extends DalResponseException {

	public final String dalErrorMessage;
	public final String url;
	public final HttpResponseInfo responseInfo;

	public DalResponseHttpException(String message, String dalErrorMessage, String url, HttpResponseInfo responseInfo) {
		super(message);
		this.dalErrorMessage = dalErrorMessage;
		this.url = url;
		this.responseInfo = responseInfo;
	}

}
