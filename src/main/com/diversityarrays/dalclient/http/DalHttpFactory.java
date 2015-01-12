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
package com.diversityarrays.dalclient.http;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import javax.net.ssl.SSLContext;

import org.apache.commons.collections15.Factory;

import com.diversityarrays.dalclient.HttpResponseInfo;
import com.diversityarrays.util.Pair;

/**
 * Provides the various factory methods required to create implementation specific instances
 * of the Dal<i>Xyz</i> interfaces.
 * @author brian
 *
 */
public interface DalHttpFactory {

	/**
	 * Create a wrapped CloseableHttpClient using the provided SSLContext.
	 * @param context
	 * @return an instance of DalCloseableHttpClient
	 */
	public DalCloseableHttpClient createCloseableHttpClient(SSLContext context);

	/**
	 * Create a wrapped ResponseHandler to process String responses.
	 * @return an instance of DalResponseHandler
	 */
	public DalResponseHandler<String> createBasicResponseHandler();

	/**
	 * Create a wrapped ResponseHandler to process HttpResponseInfo instances.
	 * @return an instance of DalResponseHandler
	 */
	public DalResponseHandler<HttpResponseInfo> createResponseHandler();

	/**
	 * Create a new DalRequest to send an Http GET request to the server.
	 * @param dalCommandUrl the full URL to the DAL server
	 * @return an instance of DalRequest
	 */
	public DalRequest createHttpGet(String dalCommandUrl);

	/**
	 * Create a new DalRequest to send an Http POST request to the server.
	 * @param dalCommandUrl the full URL to the DAL server
	 * @param collectedPairs
	 * @param charset
	 * @return a DalRequest
	 */
	public DalRequest createHttpPost(String dalCommandUrl, List<Pair<String,String>> collectedPairs, Charset charset);

	/**
	 * Create new DalRequest to upload data in the specified File.
	 * @param dalCommandUrl the full URL to the DAL server
	 * @param parameters
	 * @param rand_num
	 * @param namesInOrder
	 * @param signature
	 * @param fileForUpload
	 * @return an instance of DalRequest
	 */
	public DalRequest createForUpload(String dalCommandUrl, 
			List<Pair<String,String>> parameters, 
			String rand_num, String namesInOrder, String signature, 
			File fileForUpload);

	/**
	 * Create a new DalRequest to upload data which is provided by the factory.
	 * @param dalCommandUrl the full URL to the DAL server
	 * @param parameters
	 * @param rand_num
	 * @param namesInOrder
	 * @param signature
	 * @param factory
	 * @return an instance of DalRequest
	 */
	public DalRequest createForUpload(String dalCommandUrl,
			List<Pair<String,String>> parameters, 
			String rand_num, String namesInOrder, String signature, 
			Factory<InputStream> factory);

}
