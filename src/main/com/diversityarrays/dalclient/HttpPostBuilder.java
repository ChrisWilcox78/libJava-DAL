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
package com.diversityarrays.dalclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections15.Factory;
import org.apache.commons.logging.Log;

import com.diversityarrays.dalclient.http.DalHttpFactory;
import com.diversityarrays.dalclient.http.DalRequest;
import com.diversityarrays.util.Pair;

/**
 * <p>
 * Provides a way to create an HttpPost request to send to a DAL server.
 * The default response type is XML but may be changed using <code>setResponseType(...)</code>
 * <p>
 * Usage:
 * <pre>
 * HttpPost post = new HttpPostBuilder(&lt;url-of-dal-request&gt;)
 *    .setResponseType(ResponseType.JSON)
 *    .addParameter("_id", "345")
 *    .buildHttpPost();
 * </pre>
 * @author brian
 *
 */
public class HttpPostBuilder {
	
	private static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
	private static final Charset UTF_8 = Charset.forName("UTF-8");
	
	private DalHttpFactory dalHttpFactory;
	private String dalCommandUrl;
	private ResponseType responseType = ResponseType.XML;
	private List<Pair<String,String>> collectedPairs = new ArrayList<Pair<String,String>>();
	private Charset charset = ISO_8859_1;
	private final Log log;

	public HttpPostBuilder(DalHttpFactory dalHttpFactory, String dalCommandUrl) {
		this(dalHttpFactory, dalCommandUrl, null);
	}
	
	public HttpPostBuilder(DalHttpFactory dalHttpFactory, String dalCommandUrl, Log log) {
		this.dalHttpFactory = dalHttpFactory;
		this.dalCommandUrl = dalCommandUrl;
		this.log = log;
	}
	
	/**
	 * Set the Charset to be used to build the HTTP request.
	 * The default is ISO-8859-1.
	 * @param charset
	 * @return this HttpPostBuilder
	 */
	public HttpPostBuilder setCharset(Charset charset) {
		this.charset = charset;
		return this;
	}
	
	/**
	 * Set the ResponseType to be used by DAL in responding to this operation.
	 * @param responseType
	 * @return this HttpPostBuilder
	 */
	public HttpPostBuilder setResponseType(ResponseType responseType) {
		if (responseType.postValue==null) {
			throw new IllegalArgumentException("Invalid for setResponseType:"+responseType);
		}
		this.responseType = responseType;
		return this;
	}

	/**
	 * Add a value for the named parameter for this POST request.
	 * @param name
	 * @param value
	 * @return this HttpPostBuilder
	 */
	public HttpPostBuilder addParameter(String name, String value) {
		collectedPairs.add(new Pair<String,String>(name, value));
		return this;
	}
	
	/**
	 * Add a number of named parameters for this POST request.
	 * @param params
	 * @return this HttpPostBuilder
	 */
	public HttpPostBuilder addParameters(List<Pair<String,String>> params) {
		collectedPairs.addAll(params);
		return this;
	}
	
	/**
	 * Create and return an HttpPost instance using the supplied
	 * parameters, ResponseType etc.
	 * @return an HttpPost instance
	 */
	public DalRequest build() {
		
		// Only need to add the ctype parameter if not XML because XML
		// is the DAL server's default response format.
		if (! responseType.isXML()) {
			collectedPairs.add(new Pair<String,String>("ctype", responseType.postValue));
		}
		
		DalRequest result = dalHttpFactory.createHttpPost(dalCommandUrl, collectedPairs, UTF_8);
		
		return result;
	}
	
	/**
	 * Create and return an HttpPost instance for use in an UPDATE-class DAL operation.
	 * @param writeKey
	 * @return a DalRequest
	 */
	public DalRequest buildForUpdate(String writeKey) {
		
		List<Pair<String,String>> forPost = collectPairsForUpdate(writeKey, null);

		return dalHttpFactory.createHttpPost(dalCommandUrl, forPost, charset);
	}
	
	/**
	 * This entry is provided to support debugging.
	 * @param writeKey the token provided by DAL on a successful login
	 * @param returnDataForSignature if non-null then it receives the concatenated data values
	 * @return a List of Pair&lt;String,String&gt; 
	 */
	public List<Pair<String,String>> collectPairsForUpdate(String writeKey, StringBuilder returnDataForSignature) {
		String rand_num = DalUtil.createRandomNumberString();

		StringBuilder dataForSignature = new StringBuilder(dalCommandUrl);
		StringBuilder namesInOrder = new StringBuilder();
		
		dataForSignature.append(rand_num);
		for (Pair<String,String> pair : collectedPairs) {
			String value = pair.b;
			if (value==null) {
				// TODO resolve whether or not DAL handles null values
				// For now, null values are not handled correctly so make them "empty"
				// (for a start, the signature string gets befuddled with 'null')
				value = "";
			}
			dataForSignature.append(value);
			namesInOrder.append(pair.a).append(',');
			// TODO check about the extra comma issue
		}

		String forSignature = dataForSignature.toString();
		if (returnDataForSignature!=null) {
			returnDataForSignature.append(forSignature);
		}
		String signature = DalUtil.computeHmacSHA1(writeKey, forSignature);

		List<Pair<String,String>> forPost = new ArrayList<Pair<String,String>>(collectedPairs);
		
		forPost.add(new Pair<String,String>("rand_num", rand_num));
		forPost.add(new Pair<String,String>("url", dalCommandUrl));
		forPost.add(new Pair<String,String>("param_order", namesInOrder.toString()));
		forPost.add(new Pair<String,String>("signature", signature));

		if (! responseType.isXML()) {
			forPost.add(new Pair<String,String>("ctype", responseType.postValue));
		}
		
		if (log!=null && log.isDebugEnabled()) {
			log.debug(this.getClass().getName() + ".collectPairsForUpdate("+writeKey+")");
			for (Pair<String,String> nvp : forPost) {
				log.debug("  "+nvp.a+"="+nvp.b);
			}
		}
		
		return forPost;
	}

	/**
	 * Create an HttpPost instance for uploading a file using the other supplied
	 * parameters, ResponseType etc.
	 * @param writeKey
	 * @param fileForUpload
	 * @return a DalRequest instance
	 * @throws FileNotFoundException
	 */
	public DalRequest buildForUpload(String writeKey, File fileForUpload)
	throws FileNotFoundException
	{
		String rand_num = DalUtil.createRandomNumberString();

		StringBuilder dataForSignature = new StringBuilder(dalCommandUrl);
		dataForSignature.append(rand_num);

		String md5 = DalUtil.computeMD5checksum(new FileInputStream(fileForUpload));


		StringBuilder namesInOrderBuilder = new StringBuilder();
		for (Pair<String,String> pair : collectedPairs) {
			dataForSignature.append(pair.b);
			namesInOrderBuilder.append(pair.a).append(',');
			// TODO check about the extra comma issue
		}
		dataForSignature.append(md5);
		
		String namesInOrder = namesInOrderBuilder.toString();
		String signature = DalUtil.computeHmacSHA1(writeKey, dataForSignature.toString());
		
		if (log!=null && log.isDebugEnabled()) {
			log.debug(this.getClass().getName() + ".buildForUpload("+writeKey+" , File=" + fileForUpload.getPath() + ")");
			log.debug("  dataForSignature="+dataForSignature);
			log.debug("  param_order="+namesInOrder);
			log.debug("  signature="+signature);
			log.debug("  fileSize="+fileForUpload.length());
		}
		
		return dalHttpFactory.createForUpload(dalCommandUrl, collectedPairs, rand_num, namesInOrder, signature, fileForUpload);

	}
	
	/**
	 * Create an HttpPost instance for uploading an InputStream using the other supplied
	 * parameters, ResponseType etc.
	 * @param writeKey
	 * @param factory Factory&lt;InputStream&gt; 
	 * @return an DalRequest instance
	 */
	public DalRequest buildForUpload(String writeKey, Factory<InputStream> factory)
	{
		String rand_num = DalUtil.createRandomNumberString();
		String md5 = DalUtil.computeMD5checksum(factory.create());

		StringBuilder dataForSignature = new StringBuilder(dalCommandUrl);
		dataForSignature.append(rand_num);


		StringBuilder namesInOrderBuilder = new StringBuilder();
		for (Pair<String,String> pair : collectedPairs) {
			dataForSignature.append(pair.a);
			namesInOrderBuilder.append(pair.b).append(',');
			// TODO check about the extra comma issue
		}
		dataForSignature.append(md5);
		String namesInOrder = namesInOrderBuilder.toString();

		String signature = DalUtil.computeHmacSHA1(writeKey, dataForSignature.toString());

		if (log!=null && log.isDebugEnabled()) {
			log.debug(this.getClass().getName() + ".buildForUpload("+writeKey+" , InputStream )");
			log.debug("  dataForSignature="+dataForSignature);
			log.debug("  param_order="+namesInOrder);
			log.debug("  signature="+signature);
		}

		return dalHttpFactory.createForUpload(dalCommandUrl, collectedPairs, rand_num, namesInOrder, signature, factory);	
	}

}
