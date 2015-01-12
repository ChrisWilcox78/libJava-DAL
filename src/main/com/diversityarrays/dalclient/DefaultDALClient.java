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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.collections15.Closure;
import org.apache.commons.collections15.Factory;
import org.apache.commons.logging.Log;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.diversityarrays.dalclient.DalUtil.JsonResult;
import com.diversityarrays.dalclient.http.DalCloseableHttpClient;
import com.diversityarrays.dalclient.http.DalHeader;
import com.diversityarrays.dalclient.http.DalHttpFactory;
import com.diversityarrays.dalclient.http.DalRequest;
import com.diversityarrays.dalclient.http.DalResponseHandler;
import com.diversityarrays.util.Pair;

/**
 * <p>
 * Provide a concrete implementation of a DALClient.
 * <p>
 * In this implementation, <i>responseType</i> defaults to XML (the default DAL server behaviour),
 * <i>switchGroupOnLogin</i> defaults to true
 * and <i>sessionExpiryOption</i> defaults to AUTO_EXPIRE.
 * <p>
 * By default no logging is performed but you can enable it by default by setting the System property:<pre>
 * com.diversityarrays.dalclient.WANT_LOGGING=true
 * </pre>
 * @author brian
 *
 */
public class DefaultDALClient implements DALClient {
	
	private static final String MIME_TEXT_XML = "text/xml";
	private static final String MIME_APPLICATION_XML = "application/xml";
	
	static private boolean contentTypeIsXML(String contentType) {
		return contentType.startsWith(MIME_TEXT_XML) || contentType.startsWith(MIME_APPLICATION_XML);
	}
	
	private static final String LOGIN_PREFIX = "login/";
	private static final String SWITCH_GROUP_PREFIX = "switch/group/";

	private static final String OP2_LOGIN = LOGIN_PREFIX+"_username/_sessionFlag";
	private static final String OP0_LOGOUT = "logout";

	private static final String OP0_LIST_GROUP = "list/group";

	static public boolean DEBUG = Boolean.getBoolean(DefaultDALClient.class.getName()+".DEBUG");
	
	private Log log;

	private DalCloseableHttpClient httpClient = null;
	
	private SessionExpiryOption sessionExpiryOption = SessionExpiryOption.AUTO_EXPIRE;
	
	private String userId;
	
	private String userName;
	
	private final String baseUrl;

	private String writeToken;
	
	private ResponseType responseType = ResponseType.XML;

	private String groupName;

	private boolean inAdminGroup;

	private boolean autoSwitchGroupOnLogin = false;

	private String groupId;
	
	private final DalHttpFactory dalHttpFactory;

	public DefaultDALClient(String baseUrl) {
		String s = baseUrl;
		if (! s.endsWith("/")) {
			s = s + "/";
		}
		this.baseUrl = s;
		
		if (Boolean.getBoolean(DefaultDALClient.class.getName()+".WANT_LOGGING")) {
			try {
				Class<?> logFactoryClass = Class.forName("org.apache.commons.logging.LogFactory");
				Method getLogMethod = logFactoryClass.getDeclaredMethod("getLog", Class.class);
				log = (Log) getLogMethod.invoke(null, DALClient.class);
				// log = LogFactory.getLog(DALClient.class);
			} catch (ClassNotFoundException e) {
			} catch (NoSuchMethodException e) {
			} catch (SecurityException e) {
			} catch (IllegalAccessException e) {
			} catch (IllegalArgumentException e) {
			} catch (InvocationTargetException e) {
			}
		}
		
		String httpFactoryClassName = System.getProperty(this.getClass().getName()+".HTTP_FACTORY_CLASS_NAME");
		if (httpFactoryClassName == null) {
			if (System.getProperty("java.vm.name").equalsIgnoreCase("Dalvik")) {
				httpFactoryClassName = "com.diversityarrays.dalclient.httpandroid.AndroidDalHttpFactory";
			}
			else {
				httpFactoryClassName = "com.diversityarrays.dalclient.httpimpl.DalHttpFactoryImpl";
			}
		}
		
		try {
			Class<?> httpFactoryClass = Class.forName(httpFactoryClassName);
			if (! DalHttpFactory.class.isAssignableFrom(httpFactoryClass)) {
				throw new RuntimeException("className '"+httpFactoryClassName+"' does not implement "+DalHttpFactory.class.getName());
			}
			dalHttpFactory = (DalHttpFactory) httpFactoryClass.newInstance();
			
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	@Override
	public void setLog(Log log) {
		this.log = log;
	}

	@Override
	public String getBaseUrl() {
		return this.baseUrl;
	}
	
	@Override
	public ResponseType getResponseType() {
		return responseType;
	}

	@Override
	public DALClient setResponseType(ResponseType responseType) {
		if  (responseType.postValue==null) {
			throw new IllegalArgumentException("Unsupported for setResponseType:"+responseType);
		}
		this.responseType = responseType;
		return this;
	}

	@Override
	public boolean getAutoSwitchGroupOnLogin() {
		return autoSwitchGroupOnLogin;
	}

	@Override
	public DALClient setAutoSwitchGroupOnLogin(boolean b) {
		this.autoSwitchGroupOnLogin = b;
		return this;
	}
	
	@Override
	public SessionExpiryOption getSessionExpiryOption() {
		return sessionExpiryOption;
	}

	@Override
	public void setSessionExpiryOption(SessionExpiryOption sessionExpiryOption) {
		if (isLoggedIn()) {
			// the DAL doesn't enforce this but it seems to make sense to me
			throw new IllegalStateException("Can't change sessionExpiryOption while logged-in");
		}
		this.sessionExpiryOption = sessionExpiryOption;
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			if (isLoggedIn()) {
				logout();
			}
		}
		finally {
			super.finalize();
		}
	}

	@Override
	public String getUserId() {
		return userId;
	}
	
	@Override
	public String getUserName() {
		return userName;
	}
	
	@Override
	public String getWriteToken() {
		return writeToken;
	}
	
	@Override
	public boolean isLoggedIn() {
		return httpClient!=null;
	}
	
	@Override
	public boolean isInAdminGroup() {
		return inAdminGroup;
	}
	
	@Override
	public String getGroupId() {
		return groupId;
	}
	
	@Override
	public String getGroupName() {
		return groupName;
	}
	
	@Override
	public void logout() {
		try {
			if (isLoggedIn()) {
				String url = baseUrl + OP0_LOGOUT;
				DalRequest httpGet = dalHttpFactory.createHttpGet(url);
				DalResponseHandler<?> responseHandler = dalHttpFactory.createBasicResponseHandler();
				DalUtil.doHttp(httpClient, httpGet, responseHandler);
			}
		} catch (IOException ignore) {
		} finally {
			userId = null;
			userName = null;
			groupId = null;
			groupName = null;
			writeToken = null;
			
			if (httpClient!=null) {
				try { httpClient.close(); } catch (IOException ignore) { }
				httpClient = null;
			}
			
			logInfo("Logged out: "+baseUrl);
		}
	}
	
	protected void logInfo(Object msg) {
		if (log!=null) {
			log.info(msg);
		}
	}
	
	protected void logWarn(Object msg) {
		if (log!=null) {
			log.warn(msg);
		}
	}
	
	protected boolean logIsDebugEnabled() {
		return log!=null && log.isDebugEnabled();
	}
	
	protected void logDebug(Object msg) {
		if (log!=null) {
			log.debug(msg);
		}
	}
	
	@Override
	public void login(String username, String password)
	throws IOException, DalResponseException, DalLoginException 
	{
		
		if (isLoggedIn()) {
			throw new IllegalStateException("Already logged in");
		}
		
		String url = null;
		try {
			url = new CommandBuilder(OP2_LOGIN)
				.setParameter("_username", username)
				.setParameter("_sessionFlag", sessionExpiryOption.urlValue)
				.setPrefix(baseUrl)
				.build();
		} catch (DalMissingParameterException e) {
			throw new RuntimeException(e);
		}
		
		String rand = DalUtil.createRandomNumberString();
		String pwdUnameHash = DalUtil.computeHmacSHA1(password, username);
		String randhash = DalUtil.computeHmacSHA1(pwdUnameHash, rand);
		String signature = DalUtil.computeHmacSHA1(randhash, url);
		
		DalRequest request = new HttpPostBuilder(dalHttpFactory, url, log)
			.setResponseType(responseType)
			.addParameter("rand_num", rand)
			.addParameter("url", url)
			.addParameter("signature", signature)
			.build();
		
		DalResponseHandler<HttpResponseInfo> handler = dalHttpFactory.createResponseHandler();
		DalCloseableHttpClient tmpClient = null;
		
		try {
			tmpClient = dalHttpFactory.createCloseableHttpClient(DalUtil.createTrustingSSLContext());
			
			logInfo("performing login: "+url);
			Long[] elapsed = new Long[1];
			HttpResponseInfo result = DalUtil.doHttp(tmpClient, request, handler, elapsed);
			result.elapsedMillis = elapsed[0].longValue();
			logDebug("Elapsed ms="+result.elapsedMillis+" for "+url);
			
			DalResponse response = buildDalResponse(url, result);
			if (DEBUG) {
				response.printOn(System.out);
			}
					
			String errorMessage = response.getResponseErrorMessage();
			if (errorMessage!=null) {
				throw new DalLoginException(errorMessage);
			}
			
			this.userName = username;
			this.userId = response.getRecordFieldValue(DALClient.TAG_USER, DALClient.ATTR_USER_ID);
			this.writeToken = response.getRecordFieldValue(DALClient.TAG_WRITE_TOKEN, DALClient.ATTR_VALUE);
			
			logInfo("Logged in as id="+userId+"("+userName+") on "+baseUrl);
			
			// Ok - if we get here, we are logged in.
			httpClient = tmpClient;
			tmpClient = null;
			
			if (logIsDebugEnabled()) {
				logDebug("  userId="+userId);
				logDebug("  writeToken="+writeToken);
			}
			
			if (autoSwitchGroupOnLogin) {
				DalResponse listGroupResponse = performQuery(OP0_LIST_GROUP);
				DalResponseRecord record = listGroupResponse.getFirstRecord("SystemGroup");
				String groupId = record.rowdata.get("SystemGroupId");
				switchGroup(groupId);
			}
		}
		finally {
			if (! isLoggedIn()) {
				logWarn("Login failed for '"+username+"' on "+baseUrl);
				if (tmpClient!=null) {
					try { tmpClient.close(); }
					catch (IOException ignore) { }
					tmpClient = null;
				}
			}
		}
	}

	/**
	 * Make sure that users of this code do not do a login or logout using
	 * the performCommand() variants
	 * 
	 * @param command
	 * @throws DalResponseException if the command is a login or logout
	 */
	private void checkIfOkToPerform(String command) throws DalResponseException {

		// We don't want user's doing this directly.
		if (command.startsWith(LOGIN_PREFIX)) {
			logWarn("invalid attempt to perform '"+command+"'");
			throw new DalResponseException(command+" can't be performed (use client.login(un,pw))");
		}
		
		// We don't want user's doing this directly.
		if (command.startsWith(OP0_LOGOUT)) {
			logWarn("invalid attempt to perform '"+command+"'");
			throw new DalResponseException(command+" can't be performed (use client.logout())");
		}
		
		// We don't want user's doing this without going through switchGroup()
		if (command.startsWith(SWITCH_GROUP_PREFIX)) {
			logWarn("invalid attempt to perform '"+command+"'");
			throw new DalResponseException("Please use DALClient.switchGroup(groupId) instead of '"+command+"'");
		}
	}
	
	@Override
	public DalResponse performQuery(String command) throws IOException, DalResponseException {
		return performQueryInternal(command, true);
	}

	@Override
	public QueryBuilder prepareQuery(String command) {
		return new CommandBuilder(command, this);
	}

	private DalResponse performQueryInternal(String command, boolean needToCheck) 
	throws IOException, DalResponseException {

		String urls;
		if (command.startsWith("http:")) {
			// Hmmm. This is a hack to support the results of export commands et. al.
			urls = command;
		}
		else {
			StringBuilder sb = new StringBuilder(baseUrl);
			sb.append(command);
			if (! responseType.isXML()) {
				URL url = new URL(sb.toString());
				// User may already have appended parameters
				sb.append((url.getQuery()==null) ? '?' : '&')
					.append("ctype=").append(responseType.postValue);
			}
			urls = sb.toString();
		}
		
		if (needToCheck) {
			checkIfOkToPerform(urls.substring(baseUrl.length()));
		}
		
		logInfo("performing query: "+urls);
		
		DalRequest request = dalHttpFactory.createHttpGet(urls);
		Long[] elapsedMillis = new Long[1];
		HttpResponseInfo result = DalUtil.doHttp(httpClient, request, dalHttpFactory.createResponseHandler(), elapsedMillis);
		result.elapsedMillis = elapsedMillis[0].longValue();
		logDebug("Elapsed ms="+result.elapsedMillis+" for "+urls);
		
		return buildDalResponse(urls, result);
	}

	private DalResponse buildDalResponse(String url, HttpResponseInfo responseInfo) throws DalResponseException {
		if (responseInfo.httpErrorReason!=null) {
			StringBuilder sb = new StringBuilder("HTTP code ");
			sb.append(responseInfo.httpStatusCode).append(": ").append(responseInfo.httpErrorReason);

			String contentType = null;
			for (DalHeader h : responseInfo.headers) {
				if ("content-type".equalsIgnoreCase(h.getName())) {
					contentType = h.getValue();
					break;
				}
			}

			String dalErrorMessage = null;
			
			if ("text/plain".equals(contentType)) {
				dalErrorMessage = responseInfo.serverResponse;
			}
			else {
				if (responseType.isXML()) {
					if (contentType!=null && ! contentTypeIsXML(contentType)) {
						// TODO check if httpErrorReason=="Internal Server Error" in which case we may want to transform to something else?
						System.err.println("Warning: response content type is '"+contentType+"' for XML");
						if (DalUtil.isHttpStatusCodeOk(responseInfo.httpStatusCode)) {
							dalErrorMessage = "DAL response code="+responseInfo.httpStatusCode+" has unexpected Content-Type: '"+contentType+"'";
						}
						else {
							// Just use the error code
							dalErrorMessage = responseInfo.httpErrorReason+" (Content-Type="+contentType+")";
						}
					}
					else {
						Throwable error = null;
						try {
							Document doc = DalUtil.createXmlDocument(responseInfo.serverResponse);
							dalErrorMessage = DalUtil.getXmlDalErrorMessage(doc);
						} catch (ParserConfigurationException e) {
							error = e;
						} catch (SAXException e) {
							error = e;
						} catch (IOException e) {
							error = e;
						}

						if  (error!=null) {
							dalErrorMessage = "DAL XML response failed to parse: "+error.getMessage();
						}
					}
				}
				else {
					// TODO handle Internal error first!
					if (contentType!=null && ! (contentType.startsWith("application/json") /* || contentType.startsWith("text/json") */)) {
						System.err.println("Warning: response content type is '"+contentType+"' for JSON");
						if (DalUtil.isHttpStatusCodeOk(responseInfo.httpStatusCode)) {
							dalErrorMessage = "DAL response code="+responseInfo.httpStatusCode+" has unexpected Content-Type: '"+contentType+"'";
						}
						else {
							// Just use the error code
							dalErrorMessage = responseInfo.httpErrorReason+" (Content-Type="+contentType+")";
						}
					}
					else {
						JsonResult jsonResult = DalUtil.parseJson(url, responseInfo.serverResponse);
						dalErrorMessage = jsonResult==null ? "?invalid JSON syntax in server response" : jsonResult.getJsonlDalErrorMessage();
					}
				}
			}
			
			if (dalErrorMessage!=null) {
				sb.append(": ").append(dalErrorMessage);
			}
			String errorMessage = sb.toString();

			logWarn("Error response for '"+url+"' is "+errorMessage);
			throw new DalResponseHttpException(errorMessage, dalErrorMessage, url, responseInfo);
		}
		
		String contentType = null;
		for (DalHeader hdr : responseInfo.headers) {
			if ("content-type".equalsIgnoreCase(hdr.getName())) {
				contentType = hdr.getValue();
				break;
			}
		}
		
		DalResponse dalResponse = null;
		if (contentType==null) {
			DalResponseException dre = new DalResponseException("Unsupported response: no 'Content-Type' header");
			logWarn("Error response for '"+url+"' is "+dre.getMessage());
			throw dre;
		}
		else if (contentTypeIsXML(contentType)) {
			dalResponse = new XmlDalResponse(url, responseInfo);
		}
		else if (contentType.startsWith("application/json")) {
			dalResponse = new JsonDalResponse(url, responseInfo);
		}
		else if ("text/x-comma-separated-values".equals(contentType)) {
			dalResponse = new CsvDalResponse(url, responseInfo);
		}
		else {
			DalResponseException dre = new DalResponseException("Unsupported response: Content-Type='"+contentType+"'");
			logWarn("Error response for '"+url+"' is "+dre.getMessage());
			throw dre;
		}
		
		logInfo(dalResponse.getClass().getSimpleName()+" response rcvd for '"+url+"'");
		if (dalResponse instanceof CsvDalResponse && (logIsDebugEnabled())) {
			dalResponse.visitResults(new DalResponseRecordVisitor() {
				@Override
				public boolean visitResponseRecord(String resultTagName, DalResponseRecord record) {
					StringBuilder sb = new StringBuilder(resultTagName);
					sb.append(":");
					for (String k : record.rowdata.keySet()) {
						sb.append("\n  ").append(k).append('=').append(record.rowdata.get(k));
					}
					logDebug(sb.toString());
					return true;
				}
			});
		}
		
		return dalResponse;
	}

	@Override
	public String switchGroup(String groupId) throws IOException, DalResponseException {

		DalResponse response = performQueryInternal(SWITCH_GROUP_PREFIX + groupId, false);
		
		String err = response.getResponseErrorMessage();
		if (err==null) {
			DalResponseRecord record = response.getFirstRecord(DALClient.TAG_INFO);
			if (! record.rowdata.isEmpty()) {
				// If we got here we must be successful
				this.groupId = groupId;
				// Note: am assuming the DAL server did the right thing here!
				this.groupName = record.rowdata.get(DALClient.ATTR_GROUP_NAME);
				this.inAdminGroup = "TRUE".equalsIgnoreCase(record.rowdata.get(DALClient.ATTR_GADMIN));
			}
			logInfo("switchGroup("+groupId+"): groupName="+groupName+" inAdminGroup="+inAdminGroup);
		}
		else {
			logWarn("switchGroup("+groupId+") error: "+err);
		}
		
		return err;
	}

	private class UpdateBuilderImpl implements UpdateBuilder {
		
		private final String command;
		private final File upload;
		
		private final Factory<InputStream> uploadStream;
		
		UpdateBuilderImpl(String cmd) {
			this.command = cmd;
			
			this.upload = null;
			this.uploadStream = null;
		}
		
		UpdateBuilderImpl(String cmd, File file) {
			this.command = cmd;
			
			this.upload = file;
			this.uploadStream = null;
		}
		
		UpdateBuilderImpl(String cmd, Factory<InputStream> streamFactory) {
			this.command = cmd;
			
			this.upload = null;
			this.uploadStream = streamFactory;
		}
		
		private List<Pair<String,String>> postParameters = new ArrayList<Pair<String,String>>();
		
		@Override
		public UpdateBuilder visitPostParameters(Closure<Pair<String,String>> visitor) {
			for (Pair<String,String> nvp : postParameters) {
				visitor.execute(nvp);
			}
			return this;
		}
		
		@Override
		public UpdateBuilderImpl addPostParameter(String name, String value) {
			postParameters.add(new Pair<String,String>(name, value));
			return this;
		}
		
		@Override
		public UpdateBuilder addPostParameter(String name, Number value) {
			return addPostParameter(name, value.toString());
		}

		@Override
		public UpdateBuilder addPostParameters(Map<String, String> postParams) {
			for (String name : postParams.keySet()) {
				postParameters.add(new Pair<String,String>(name, postParams.get(name)));
			}
			return this;
		}

		
		@Override
		public DalResponse execute() throws IOException, DalResponseException {
			
			checkIfOkToPerform(command);
			
			String url = baseUrl + command;
			
			HttpPostBuilder builder = new HttpPostBuilder(dalHttpFactory, url, log)
				.setResponseType(responseType)
				.addParameters(postParameters);
			
			DalRequest request;
			
			if (upload!=null) {
				request = builder.buildForUpload(writeToken, upload);
			}
			else if (uploadStream!=null) {
				request = builder.buildForUpload(writeToken, uploadStream);
			}
			else {
				request = builder.buildForUpdate(writeToken);
			}
			
			if (postParameters == null || postParameters.isEmpty()) {
				logInfo("UpdateBuilder.execute: NO parameters : "+url);
			}
			else {
				logInfo("UpdateBuilder.execute: " + postParameters.size() + " parameters : "+url);
				if (logIsDebugEnabled()) {
					for (Iterator<Pair<String,String>> iterator = postParameters.iterator(); iterator.hasNext(); ) {
						Pair<String,String> nvp = iterator.next();
						logDebug("  "+nvp.a+"="+nvp.b);
					}
				}
			}
			Long[] elapsed = new Long[1];
			HttpResponseInfo result = DalUtil.doHttp(httpClient, request, dalHttpFactory.createResponseHandler(), elapsed);
			result.elapsedMillis = elapsed[0].longValue();
			logDebug("Elapsed ms="+result.elapsedMillis+" for "+url);
			
			return buildDalResponse(url, result);
		}
		
		@Override
		public UpdateBuilder printOn(PrintStream ps) {
			ps.println("Command: "+command);
			ps.println("Upload File: "+(upload==null?"null":upload.getPath()));
			int nParameters = postParameters.size();
			ps.println("Parameters: "+nParameters);
			
			String dalCommandUrl = baseUrl + command;
			
			HttpPostBuilder builder = new HttpPostBuilder(dalHttpFactory, dalCommandUrl) // No logging here !
				.setResponseType(responseType)
				.addParameters(postParameters);
			
			StringBuilder dataForSignature = new StringBuilder("Data for Signature:\n");
			List<Pair<String,String>> pairs = builder.collectPairsForUpdate(writeToken, dataForSignature);
			
			ps.println("Pairs for Update: "+pairs.size());
			int count = 0;
			for (Pair<String,String> pair : pairs) {
				ps.println("  "+(++count)+": "+pair.a+"="+pair.b);
				if (count==nParameters) {
					ps.println("---- computed ------");
				}
			}
			
			ps.println(dataForSignature);
			return this;
		}

	}
	
	/**
	 * Perform an EXPORT command using the Fluent programming style.
	 * This is a convenience method which calls performUpdate(...);
	 * <P>
	 * EXPORT commands start with "export/"
	 * <P>Usage:<pre>
	 * DalResponse response = client.prepareUpdate(<i>export-command</i>)
	 *    .addPostParameter(name1, value1)
	 *    .addPostParameter(name2, value2)
	 *     :
	 *    .execute();
	 * </pre>
	 * @param command
	 * @return an UpdateBuilder instance
	 */
	@Override
	public UpdateBuilder prepareExport(String command) {
		return new UpdateBuilderImpl(command);
	}

	/**
	 * Perform an EXPORT command with the provided parameters.
	 * This is a convenience method which calls performUpdate(...);
	 * <P>
	 * EXPORT commands start with "export/".
	 * <P>Usage:
	 * <pre>
	 *   Map&lt;String,String&gt; params = new LinkedHashMap&lt;String,String&gt;();
	 *   params.put("name1", "value1");
	 *   params.put("name2", "value2");
	 *   DalResponse response = client.performUpdate("export/genotype", params);
	 * </pre>
	 * @param command
	 * @param postParameters
	 * @return a DalResponse instance
	 * @throws IOException
	 * @throws DalResponseException 
	 */
	@Override
	public DalResponse performExport(String command, Map<String,String> postParameters)
	throws IOException, DalResponseException
	{
		return performUpdate(command, postParameters);
	}

	/**
	 * Perform an UPDATE command using the Fluent programming style.
	 * UPDATE commands are those like "add", "delete", etc.
	 * <p>
	 * Usage:<pre>
	 * DalResponse response = client.prepareUpdate("update-command')
	 *    .addPostParameter(name1, value1)
	 *    .addPostParameter(name2, value2)
	 *     :
	 *    .execute();
	 * </pre>
	 * @param command
	 * @return an UpdateBuilder instance
	 */
	@Override
	public UpdateBuilder prepareUpdate(String command) {
		return new UpdateBuilderImpl(command);
	}

	/**
	 * Perform an UPDATE command with the provided parameters.
	 * UPDATE commands are those like "add", "delete", etc.
	 * <p>
	 * Usage:<pre>
	 *   Map&lt;String,String&gt; params = new LinkedHashMap&lt;String,String&gt;();
	 *   params.put("name1", "value1");
	 *   params.put("name2", "value2");
	 *   DalResponse response = client.performUpdate("update-command", params);
	 * </pre>
	 * @param command
	 * @param postParameters may be null
	 * @return a DalResponse instance
	 * @throws IOException
	 * @throws DalResponseException 
	 */
	@Override
	public DalResponse performUpdate(String command, Map<String,String> postParameters)
	throws IOException, DalResponseException
	{
		checkIfOkToPerform(command);
		
		String url = baseUrl + command;
		
		HttpPostBuilder postBuilder = new HttpPostBuilder(dalHttpFactory, url, log).setResponseType(responseType);
		if (postParameters!=null) {
			for (Map.Entry<String, String> e: postParameters.entrySet()) {
				postBuilder.addParameter(e.getKey(), e.getValue());
			}
		}
		
		DalRequest request = postBuilder.buildForUpdate(writeToken);

		Long[] elapsed = new Long[1];
		logInfo("performUpdate: "+url);
		HttpResponseInfo result = DalUtil.doHttp(httpClient, request, dalHttpFactory.createResponseHandler(), elapsed);
		result.elapsedMillis = elapsed[0].longValue();
		logDebug("Elapsed ms="+result.elapsedMillis+" for "+url);
		
		return buildDalResponse(url, result);
		//   returns "ReturnId/@Value
	}
	
	// File Upload commands

	/**
	 * Perform a file upload command using the Fluent programming style.
	 * <p>
	 * Usage:<pre>
	 * DalResponse response = client.prepareUpdate("update-command', fileToUpload)
	 *    .addPostParameter(name1, value1)
	 *    .addPostParameter(name2, value2)
	 *     :
	 *    .execute();
	 * </pre>
	 * @param command
	 * @return a UpdateBuilder instance
	 */
	@Override
	public UpdateBuilder prepareUpload(String command, File upload) {
		return new UpdateBuilderImpl(command, upload);
	}

	/**
	 * Perform an upload command for an InputStream using the Fluent programming style.
	 * @param command
	 * @param stream
	 * @return an UpdateBuilder instance
	 */
	@Override
	public UpdateBuilder prepareUpload(String command, Factory<InputStream> stream) {
		return new UpdateBuilderImpl(command, stream);
	}
	
	/**
	 * Perform a file upload command with the provided parameters.
	 * <p>
	 * Usage:<pre>
	 *   Map&lt;String,String&gt; params = new LinkedHashMap&lt;String,String&gt;();
	 *   params.put("name1", "value1");
	 *   params.put("name2", "value2");
	 *   DalResponse response = client.performUpdate("update-command", params, fileToUpload);
	 * </pre>
	 * @param command
	 * @param postParameters
	 * @param upload
	 * @return a DalResponse instance
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws DalResponseException 
	 */
	@Override
	public DalResponse performUpload(String command, 
			Map<String,String> postParameters,
			File upload)
	throws FileNotFoundException, IOException, DalResponseException {
		
		checkIfOkToPerform(command);
		
		String url = baseUrl + command;
		
		HttpPostBuilder postBuilder = new HttpPostBuilder(dalHttpFactory, url, log).setResponseType(responseType);
		for (Map.Entry<String, String> e: postParameters.entrySet()) {
			postBuilder.addParameter(e.getKey(), e.getValue());
		}
		
		DalRequest request = postBuilder.buildForUpload(writeToken, upload);
		
		logInfo("performUpload: "+url);
		Long[] elapsed = new Long[1];
		HttpResponseInfo result = DalUtil.doHttp(httpClient, request, dalHttpFactory.createResponseHandler(), elapsed);
		result.elapsedMillis = elapsed[0].longValue();
		logDebug("Elapsed ms="+result.elapsedMillis+" for "+url);
		
		return buildDalResponse(url, result);
		// returns "ReturnIdFile/@xml"
	}

	/**
	 * Perform an upload command for InputStream data with the provided parameters.
	 * For example:<pre>
	 *   String content = "&lt;DATA&gt;&lt;/DATA&gt;";
	 *   Factory&lt;InputStream&gt; factory = new Factory&lt;InputStream&gt;() {
	 *      public InputStream create() {
	 *         return new com.diversityarrays.util.StringInputStream(content); 
	 *      }
	 *   };
	 *   Map&lt;String,String&gt; params = new LinkedHashMap&lt;String,String&gt;();
	 *   params.put("name1", "value1");
	 *   params.put("name2", "value2");
	 *   DalResponse response = client.performUpdate("update-command", params, factory, "xml_data");
	 * </pre>
	 * @param command
	 * @param postParameters
	 * @param streamFactory
	 * @return a DalResponse
	 * @throws DalResponseException
	 * @throws IOException
	 */
	public DalResponse performUpload(String command,
			Map<String, String> postParameters,
			Factory<InputStream> streamFactory) 
	throws DalResponseException, IOException {
		
		checkIfOkToPerform(command);
		
		String url = baseUrl + command;
		
		HttpPostBuilder postBuilder = new HttpPostBuilder(dalHttpFactory, url, log).setResponseType(responseType);
		for (Map.Entry<String, String> e: postParameters.entrySet()) {
			postBuilder.addParameter(e.getKey(), e.getValue());
		}
		
		DalRequest request = postBuilder.buildForUpload(writeToken, streamFactory);
		
		logInfo("performUpload: "+url);
		Long[] elapsed = new Long[1];
		HttpResponseInfo result = DalUtil.doHttp(httpClient, request, dalHttpFactory.createResponseHandler(), elapsed);
		result.elapsedMillis = elapsed[0].longValue();
		logDebug("Elapsed ms="+result.elapsedMillis+" for "+url);
		
		return buildDalResponse(url, result);
	}

}
