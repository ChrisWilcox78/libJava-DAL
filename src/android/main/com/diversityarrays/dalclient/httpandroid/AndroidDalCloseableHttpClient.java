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

import java.io.Closeable;
import java.io.IOException;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;


import ch.boye.httpclientandroidlib.client.methods.CloseableHttpResponse;
import ch.boye.httpclientandroidlib.impl.client.CloseableHttpClient;
import ch.boye.httpclientandroidlib.client.protocol.HttpClientContext;
import ch.boye.httpclientandroidlib.client.CookieStore;
import ch.boye.httpclientandroidlib.cookie.Cookie;

import com.diversityarrays.dalclient.http.DalCloseableHttpClient;
import com.diversityarrays.dalclient.http.DalCloseableHttpResponse;
import com.diversityarrays.dalclient.http.DalRequest;

public class AndroidDalCloseableHttpClient implements DalCloseableHttpClient, Closeable {

	private CloseableHttpClient client;
    private final List<HttpCookie> httpCookies = new ArrayList<>();

	public AndroidDalCloseableHttpClient(CloseableHttpClient client) {
		this.client = client;
	}

	@Override
	public List<HttpCookie> getHttpCookies() {
	    return httpCookies;
	}

	@Override
	public void close() throws IOException {
		client.close();
	}

	@Override
	public DalCloseableHttpResponse execute(DalRequest request) throws IOException {

		AndroidDalRequest androidRequest = (AndroidDalRequest) request;

		HttpClientContext context = HttpClientContext.create();
		CloseableHttpResponse response = client.execute(androidRequest.httpRequest, context);
		CookieStore cookieStore = context.getCookieStore();

		List<HttpCookie> list = new ArrayList<>();
        if (cookieStore != null) {
            for (Cookie cookie : cookieStore.getCookies()) {
                list.add(new HttpCookie(cookie.getName(), cookie.getValue()));
            }
        }
        httpCookies.clear();
        httpCookies.addAll(list);

        return new AndroidDalCloseableResponse(response);
	}
}
