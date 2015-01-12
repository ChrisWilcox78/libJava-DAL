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
package com.diversityarrays.dalclient.httpimpl;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

import com.diversityarrays.dalclient.http.DalCloseableHttpResponse;
import com.diversityarrays.dalclient.http.DalHeader;

/**
 * Provide an implementation of DalCloseableHttpResponse for use with standard apache http libraries.
 * @author brian
 *
 */
public class DalCloseableResponseImpl implements DalCloseableHttpResponse {

	private CloseableHttpResponse response;

	public DalCloseableResponseImpl(CloseableHttpResponse response) {
		this.response = response;
	}

	public CloseableHttpResponse getCloseableHttpResponse() {
		return response;
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
