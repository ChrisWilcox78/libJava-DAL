/*
 * dalclient library - provides utilities to assist in using KDDart-DAL servers
 * Copyright (C) 2015,2016,2017  Diversity Arrays Technology
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
package com.diversityarrays.dalclient.httpandroid;

import java.io.IOException;

import ch.boye.httpclientandroidlib.client.ResponseHandler;

import com.diversityarrays.dalclient.http.DalCloseableHttpResponse;
import com.diversityarrays.dalclient.http.DalResponseHandler;

public class AndroidDalResponseHandler<T> implements DalResponseHandler<T> {

	private ResponseHandler<T> responseHandler;

	public AndroidDalResponseHandler(ResponseHandler<T> responseHandler) {
		this.responseHandler = responseHandler;
	}

	@Override
	public T handleResponse(DalCloseableHttpResponse response) throws IOException {
		
		AndroidDalCloseableResponse androidResponse = (AndroidDalCloseableResponse) response;
		
		return responseHandler.handleResponse(androidResponse.response);
	}

}
