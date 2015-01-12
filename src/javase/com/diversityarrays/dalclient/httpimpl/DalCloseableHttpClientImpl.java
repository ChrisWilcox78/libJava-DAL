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

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;

import com.diversityarrays.dalclient.http.DalCloseableHttpClient;
import com.diversityarrays.dalclient.http.DalCloseableHttpResponse;
import com.diversityarrays.dalclient.http.DalRequest;

/**
* Provide an implementation of DalCloseableHttpClient for use with standard apache http libraries.
 * @author brian
 *
 */
public class DalCloseableHttpClientImpl implements DalCloseableHttpClient {

	private CloseableHttpClient client;

	public DalCloseableHttpClientImpl(CloseableHttpClient client) {
		this.client = client;
	}

	@Override
	public void close() throws IOException {
		client.close();
	}

	@Override
	public DalCloseableHttpResponse execute(DalRequest request) throws IOException {
		
		DalRequestImpl androidRequest = (DalRequestImpl) request;
		
		CloseableHttpResponse response = client.execute(androidRequest.httpRequest);
		
		return new DalCloseableResponseImpl(response);
	}
}
