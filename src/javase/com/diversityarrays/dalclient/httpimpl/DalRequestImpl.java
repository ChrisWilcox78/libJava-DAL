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

import java.net.URI;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpUriRequest;

import com.diversityarrays.dalclient.http.DalHeader;
import com.diversityarrays.dalclient.http.DalRequest;

/**
 * Provide an implementation of DalRequest for use with standard apache http libraries.
 * @author brian
 *
 */
public class DalRequestImpl implements DalRequest {

	HttpUriRequest httpRequest;
	
	public DalRequestImpl(HttpUriRequest httpGet) {
		httpRequest = httpGet;
	}
	
	@Override
	public URI getURI() {
		return httpRequest.getURI();
	}

	@Override
	public DalHeader[] getAllHeaders() {
		
		Header[] headers = httpRequest.getAllHeaders();
		
		DalHeader[] result = new DalHeader[headers.length];
		for (int i = headers.length; --i >= 0; ) {
			Header h = headers[i];
			result[i] = new DalHeader(h.getName(), h.getValue());
		}
		return result;
	}

}
