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
package com.diversityarrays.dalclient.httpandroid;

import java.io.IOException;

import ch.boye.httpclientandroidlib.Header;
import ch.boye.httpclientandroidlib.HttpEntity;
import ch.boye.httpclientandroidlib.client.methods.CloseableHttpResponse;
import ch.boye.httpclientandroidlib.util.EntityUtils;

import com.diversityarrays.dalclient.http.DalCloseableHttpResponse;
import com.diversityarrays.dalclient.http.DalHeader;

public class AndroidDalCloseableResponse implements DalCloseableHttpResponse {

	CloseableHttpResponse response;

	public AndroidDalCloseableResponse(CloseableHttpResponse response) {
		this.response = response;
	}

	@Override
	public void close() throws IOException {
		response.close();		
	}

	@Override
	public DalHeader[] getAllHeaders() {
		Header[] headers = response.getAllHeaders();
		DalHeader[] result = new DalHeader[headers.length];
		for (int i = headers.length; --i >= 0; ) {
			Header h = headers[i];
			result[i] = new DalHeader(h.getName(), h.getValue());
		}
		return result;
	}

	@Override
	public int getStatusCode() {
		return response.getStatusLine().getStatusCode();
	}

	@Override
	public String getReasonPhrase() {
		return response.getStatusLine().getReasonPhrase();
	}

	@Override
	public String getEntityAsString() throws IOException {
		HttpEntity entity = response.getEntity();
		return entity != null ? EntityUtils.toString(entity) : null;
	}
}
