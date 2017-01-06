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
package com.diversityarrays.dalclient.httpimpl;

import java.io.IOException;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
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
	private final List<HttpCookie> httpCookies = new ArrayList<>();

	public DalCloseableHttpClientImpl(CloseableHttpClient client) {
		this.client = client;
	}

	@Override
	public void close() throws IOException {
		client.close();
	}

	@Override
	public List<HttpCookie> getHttpCookies() {
	    return Collections.unmodifiableList(httpCookies);
	}

	@Override
	public DalCloseableHttpResponse execute(DalRequest request) throws IOException {
		DalRequestImpl requestImpl = (DalRequestImpl) request;

		HttpClientContext context = HttpClientContext.create();
        CloseableHttpResponse response = client.execute(requestImpl.httpRequest, context);

        CookieStore cookieStore = context.getCookieStore();
        List<HttpCookie> list = new ArrayList<>();
        if (cookieStore != null) {
            for (Cookie cookie : cookieStore.getCookies()) {
                list.add(new HttpCookie(cookie.getName(), cookie.getValue()));
            }
        }
        httpCookies.clear();
        httpCookies.addAll(list);

		return new DalCloseableResponseImpl(response);
	}
}
