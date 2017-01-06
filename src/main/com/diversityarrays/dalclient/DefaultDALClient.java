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
package com.diversityarrays.dalclient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpCookie;
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
import com.diversityarrays.dalclient.util.Pair;

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
@SuppressWarnings("nls")
public class DefaultDALClient implements DALClient {

	private static final String MIME_TEXT_X_COMMA_SEPARATED_VALUES = "text/x-comma-separated-values"; //$NON-NLS-1$
    private static final String MIME_APPLICATION_JSON = "application/json"; //$NON-NLS-1$
    private static final String MIME_TEXT_XML = "text/xml"; //$NON-NLS-1$
	private static final String MIME_APPLICATION_XML = "application/xml"; //$NON-NLS-1$

	static private boolean contentTypeIsXML(String contentType) {
		return contentType.startsWith(MIME_TEXT_XML) || contentType.startsWith(MIME_APPLICATION_XML);
	}

	private static final String LOGIN_PREFIX = "login/"; //$NON-NLS-1$
	private static final String SWITCH_GROUP_PREFIX = "switch/group/"; //$NON-NLS-1$

	private static final String OP2_LOGIN = LOGIN_PREFIX+"_username/_sessionFlag"; //$NON-NLS-1$
	private static final String OP0_LOGOUT = "logout"; //$NON-NLS-1$

	private static final String OP0_LIST_GROUP = "list/group"; //$NON-NLS-1$

	static public boolean DEBUG = Boolean.getBoolean(DefaultDALClient.class.getName()+".DEBUG"); //$NON-NLS-1$

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
		if (! s.endsWith("/")) { //$NON-NLS-1$
			s = s + "/"; //$NON-NLS-1$
		}
		this.baseUrl = s;

		if (Boolean.getBoolean(DefaultDALClient.class.getName()+".WANT_LOGGING")) { //$NON-NLS-1$
			try {
				Class<?> logFactoryClass = Class.forName("org.apache.commons.logging.LogFactory"); //$NON-NLS-1$
				Method getLogMethod = logFactoryClass.getDeclaredMethod("getLog", Class.class); //$NON-NLS-1$
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

		String httpFactoryClassName = System.getProperty(this.getClass().getName()+".HTTP_FACTORY_CLASS_NAME"); //$NON-NLS-1$
		if (httpFactoryClassName == null) {
			if (System.getProperty("java.vm.name").equalsIgnoreCase("Dalvik")) { //$NON-NLS-1$ //$NON-NLS-2$
				httpFactoryClassName = "com.diversityarrays.dalclient.httpandroid.AndroidDalHttpFactory"; //$NON-NLS-1$
			}
			else {
				httpFactoryClassName = "com.diversityarrays.dalclient.httpimpl.DalHttpFactoryImpl"; //$NON-NLS-1$
			}
		}

		try {
			Class<?> httpFactoryClass = Class.forName(httpFactoryClassName);
			if (! DalHttpFactory.class.isAssignableFrom(httpFactoryClass)) {
				throw new RuntimeException("className '" //$NON-NLS-1$
				        + httpFactoryClassName+"' does not implement " //$NON-NLS-1$
				        + DalHttpFactory.class.getName());
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
	/**
	 * Retrieve the HttpCookies from the last request.
	 * @return a List of HttpCookie
	 * @throws IllegalStateException if not logged in
	 */
	public List<HttpCookie> getHttpCookies() throws IllegalStateException {
	    if (! isLoggedIn()) {
	        throw new IllegalStateException("Not logged in");
	    }
	    return httpClient.getHttpCookies();
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

			logInfo("Logged out: "+baseUrl); //$NON-NLS-1$
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
				.setParameter("_username", username) //$NON-NLS-1$
				.setParameter("_sessionFlag", sessionExpiryOption.urlValue) //$NON-NLS-1$
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
			.addParameter("rand_num", rand) //$NON-NLS-1$
			.addParameter("url", url) //$NON-NLS-1$
			.addParameter("signature", signature) //$NON-NLS-1$
			.build();

		DalResponseHandler<HttpResponseInfo> handler = dalHttpFactory.createResponseHandler();
		DalCloseableHttpClient tmpClient = null;

		try {
			tmpClient = dalHttpFactory.createCloseableHttpClient(DalUtil.createTrustingSSLContext());

			logInfo("performing login: "+url); //$NON-NLS-1$
			Long[] elapsed = new Long[1];
			HttpResponseInfo result = DalUtil.doHttp(tmpClient, request, handler, elapsed);
			result.elapsedMillis = elapsed[0].longValue();
			logDebug("Elapsed ms="+result.elapsedMillis+" for "+url); //$NON-NLS-1$ //$NON-NLS-2$

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

			logInfo("Logged in as id="+userId+"("+userName+") on "+baseUrl); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			// Ok - if we get here, we are logged in.
			httpClient = tmpClient;
			tmpClient = null;

			if (logIsDebugEnabled()) {
				logDebug("  userId="+userId); //$NON-NLS-1$
				logDebug("  writeToken="+writeToken); //$NON-NLS-1$
			}

			if (autoSwitchGroupOnLogin) {
				DalResponse listGroupResponse = performQuery(OP0_LIST_GROUP);
				DalResponseRecord record = listGroupResponse.getFirstRecord("SystemGroup"); //$NON-NLS-1$
				String groupId = record.rowdata.get("SystemGroupId"); //$NON-NLS-1$

				String err = switchGroup(groupId);
				if (err != null) {
				    throw new DalLoginException(
				            String.format("switchGroup(%s) failed: %s", groupId, err));
				}
			}
		}
		finally {
			if (! isLoggedIn()) {
				logWarn("Login failed for '"+username+"' on "+baseUrl); //$NON-NLS-1$ //$NON-NLS-2$
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
			logWarn("invalid attempt to perform '"+command+"'"); //$NON-NLS-1$ //$NON-NLS-2$
			throw new DalResponseException(command+" can't be performed (use client.login(un,pw))");
		}

		// We don't want user's doing this directly.
		if (command.startsWith(OP0_LOGOUT)) {
			logWarn("invalid attempt to perform '"+command+"'"); //$NON-NLS-1$ //$NON-NLS-2$
			throw new DalResponseException(command+" can't be performed (use client.logout())");
		}

		// We don't want user's doing this without going through switchGroup()
		if (command.startsWith(SWITCH_GROUP_PREFIX)) {
			logWarn("invalid attempt to perform '"+command+"'"); //$NON-NLS-1$ //$NON-NLS-2$
			throw new DalResponseException("Please use DALClient.switchGroup(groupId) instead of '"+command+"'");
		}
	}

	@Override
	public PostBuilder preparePostQuery(String command) {
		return new PostBuilderImpl(command);
	}

	@Override
	public DalResponse performQuery(String command) throws IOException, DalResponseException {
		return performQueryInternal(command, true);
	}

	@Override
	public QueryBuilder prepareQuery(String command) {
		return prepareGetQuery(command);
	}

	@Override
	public QueryBuilder prepareGetQuery(String command) {
		return new CommandBuilder(command, this);
	}

	private DalResponse performQueryInternal(String command, boolean needToCheck)
	throws IOException, DalResponseException {

		String urls;
		if (command.startsWith("http:")) { //$NON-NLS-1$
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
					.append("ctype=").append(responseType.postValue); //$NON-NLS-1$
			}
			urls = sb.toString();
		}

		if (needToCheck) {
			checkIfOkToPerform(urls.substring(baseUrl.length()));
		}

		logInfo("performing query: "+urls); //$NON-NLS-1$

		DalRequest request = dalHttpFactory.createHttpGet(urls);
		Long[] elapsedMillis = new Long[1];
		HttpResponseInfo result = DalUtil.doHttp(httpClient, request, dalHttpFactory.createResponseHandler(), elapsedMillis);
		result.elapsedMillis = elapsedMillis[0].longValue();
		logDebug("Elapsed ms="+result.elapsedMillis+" for "+urls); //$NON-NLS-1$ //$NON-NLS-2$

		return buildDalResponse(urls, result);
	}

	private DalResponse buildDalResponse(String url, HttpResponseInfo responseInfo) throws DalResponseException {
		if (responseInfo.httpErrorReason!=null) {
			StringBuilder sb = new StringBuilder("HTTP code "); //$NON-NLS-1$
			sb.append(responseInfo.httpStatusCode).append(": ").append(responseInfo.httpErrorReason); //$NON-NLS-1$

			String contentType = null;
			for (DalHeader h : responseInfo.headers) {
				if ("content-type".equalsIgnoreCase(h.getName())) { //$NON-NLS-1$
					contentType = h.getValue();
					break;
				}
			}

			String dalErrorMessage = null;

			if ("text/plain".equals(contentType)) { //$NON-NLS-1$
				dalErrorMessage = responseInfo.serverResponse;
			}
			else {
				if (responseType.isXML()) {
					if (contentType!=null && ! contentTypeIsXML(contentType)) {
						// TODO check if httpErrorReason=="Internal Server Error" in which case we may want to transform to something else?
							System.err.println("Warning: response content type is '"+contentType+"' for XML");
							if (DalUtil.isHttpStatusCodeOk(responseInfo.httpStatusCode)) {
								dalErrorMessage = "DAL response code=" +
								        responseInfo.httpStatusCode +
								        " has unexpected Content-Type: "
								        + "'" + contentType + "'";
							}
							else {
								// Just use the error code
								dalErrorMessage = responseInfo.httpErrorReason
								        + " (Content-Type=" + contentType + ")"; //$NON-NLS-1$ //$NON-NLS-2$
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
							dalErrorMessage = "DAL XML response failed to parse: " + error.getMessage();
						}
					}
				}
				else {
					// TODO handle Internal error first!
					if (contentType!=null && ! (contentType.startsWith(MIME_APPLICATION_JSON) /* || contentType.startsWith("text/json") */)) {
						System.err.println("Warning: response content type is '" + contentType + "' for JSON");
						if (DalUtil.isHttpStatusCodeOk(responseInfo.httpStatusCode)) {
							dalErrorMessage = "DAL response code="
							        + responseInfo.httpStatusCode
							        + " has unexpected Content-Type: "
							        + "'" + contentType + "'"; //$NON-NLS-1$ //$NON-NLS-2$
						}
						else {
							// Just use the error code
							dalErrorMessage = responseInfo.httpErrorReason
							        + " (Content-Type=" + contentType + ")"; //$NON-NLS-1$ //$NON-NLS-2$
						}
					}
					else {
						JsonResult jsonResult = DalUtil.parseJson(url, responseInfo.serverResponse);
						dalErrorMessage = jsonResult==null
						        ? "?invalid JSON syntax in server response"  //$NON-NLS-1$
						        : jsonResult.getJsonlDalErrorMessage();
					}
				}
			}

			if (dalErrorMessage!=null) {
				sb.append(": ").append(dalErrorMessage);
			}
			String errorMessage = sb.toString();

			logWarn("Error response for "
			        + "'" + url + "'" //$NON-NLS-1$ //$NON-NLS-2$
			        + " is " + errorMessage);
			throw new DalResponseHttpException(errorMessage, dalErrorMessage, url, responseInfo);
		}

		String contentType = null;
		for (DalHeader hdr : responseInfo.headers) {
			if ("content-type".equalsIgnoreCase(hdr.getName())) { //$NON-NLS-1$
				contentType = hdr.getValue();
				break;
			}
		}

		DalResponse dalResponse = null;
		if (contentType==null) {
			DalResponseException dre = new DalResponseException(
			        "Unsupported response: no 'Content-Type' header");
			logWarn("Error response for '"+url+"' is "+dre.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			throw dre;
		}
		else if (contentTypeIsXML(contentType)) {
			dalResponse = new XmlDalResponse(url, responseInfo);
		}
		else if (contentType.startsWith(MIME_APPLICATION_JSON)) {
			dalResponse = new JsonDalResponse(url, responseInfo);
		}
		else if (MIME_TEXT_X_COMMA_SEPARATED_VALUES.equals(contentType)) {
			dalResponse = new CsvDalResponse(url, responseInfo);
		}
		else {
			DalResponseException dre = new DalResponseException(
			        "Unsupported response: Content-Type="
			                + "'" + contentType + "'"); //$NON-NLS-1$ //$NON-NLS-2$
			logWarn("Error response for '"+url+"' is "+dre.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			throw dre;
		}

		logInfo(dalResponse.getClass().getSimpleName()+" response rcvd for '"+url+"'"); //$NON-NLS-1$ //$NON-NLS-2$
		if (dalResponse instanceof CsvDalResponse && (logIsDebugEnabled())) {
			dalResponse.visitResults(new DalResponseRecordVisitor() {
				@Override
				public boolean visitResponseRecord(String resultTagName, DalResponseRecord record) {
					StringBuilder sb = new StringBuilder(resultTagName);
					sb.append(":"); //$NON-NLS-1$
					for (String k : record.rowdata.keySet()) {
						sb.append("\n  ").append(k) //$NON-NLS-1$
						.append('=').append(record.rowdata.get(k));
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
				this.inAdminGroup = "TRUE".equalsIgnoreCase(record.rowdata.get(DALClient.ATTR_GADMIN)); //$NON-NLS-1$
			}
			logInfo("switchGroup("+groupId+"): groupName="+groupName+" inAdminGroup="+inAdminGroup); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		else {
			logWarn("switchGroup("+groupId+") error: "+err); //$NON-NLS-1$ //$NON-NLS-2$
		}

		return err;
	}

	private class PostBuilderImpl implements UpdateBuilder {

		private final String command;
		private final File upload;

		private final Factory<InputStream> uploadStream;

		PostBuilderImpl(String cmd) {
			this.command = cmd;

			this.upload = null;
			this.uploadStream = null;
		}

		PostBuilderImpl(String cmd, File file) {
			this.command = cmd;

			this.upload = file;
			this.uploadStream = null;
		}

		PostBuilderImpl(String cmd, Factory<InputStream> streamFactory) {
			this.command = cmd;

			this.upload = null;
			this.uploadStream = streamFactory;
		}

		private List<Pair<String,String>> postParameters = new ArrayList<>();

		@Override
		public PostBuilder visitPostParameters(Closure<Pair<String,String>> visitor) {
			for (Pair<String,String> nvp : postParameters) {
				visitor.execute(nvp);
			}
			return this;
		}

		@Override
		public PostBuilder addPostParameter(String name, String value) {
			postParameters.add(new Pair<>(name, value));
			return this;
		}

		@Override
		public PostBuilder addPostParameter(String name, Number value) {
			return addPostParameter(name, value.toString());
		}

		@Override
		public PostBuilder addPostParameters(Map<String, String> postParams) {
			for (String name : postParams.keySet()) {
				postParameters.add(new Pair<>(name, postParams.get(name)));
			}
			return this;
		}

		@Override
		public DalResponse executeQuery() throws IOException, DalResponseException {

			checkIfOkToPerform(command);

			String url = baseUrl + command;

			HttpPostBuilder builder = new HttpPostBuilder(dalHttpFactory, url, log)
				.setResponseType(responseType)
				.addParameters(postParameters);

			DalRequest request = builder.build();

			if (postParameters == null || postParameters.isEmpty()) {
				logInfo("PostBuilderImpl.execute: NO parameters : "+url); //$NON-NLS-1$
			}
			else {
				logInfo("PostBuilderImpl.execute: " + postParameters.size() + " parameters : "+url); //$NON-NLS-1$ //$NON-NLS-2$
				if (logIsDebugEnabled()) {
					for (Iterator<Pair<String,String>> iterator = postParameters.iterator(); iterator.hasNext(); ) {
						Pair<String,String> nvp = iterator.next();
						logDebug("  "+nvp.a+"="+nvp.b); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			}
			Long[] elapsed = new Long[1];
			HttpResponseInfo result = DalUtil.doHttp(httpClient, request, dalHttpFactory.createResponseHandler(), elapsed);
			result.elapsedMillis = elapsed[0].longValue();
			logDebug("Elapsed ms="+result.elapsedMillis+" for "+url); //$NON-NLS-1$ //$NON-NLS-2$

			return buildDalResponse(url, result);
		}

		@Override
		public DalResponse execute() throws IOException, DalResponseException {
			return executeUpdate();
		}

		@Override
		public DalResponse executeUpdate() throws IOException, DalResponseException {

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
				logInfo("PostBuilderImpl.execute: NO parameters : "+url); //$NON-NLS-1$
			}
			else {
				logInfo("PostBuilderImpl.execute: " + postParameters.size()  //$NON-NLS-1$
				    + " parameters : "+url); //$NON-NLS-1$
				if (logIsDebugEnabled()) {
					for (Iterator<Pair<String,String>> iterator = postParameters.iterator(); iterator.hasNext(); ) {
						Pair<String,String> nvp = iterator.next();
						logDebug("  "+nvp.a+"="+nvp.b); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			}
			Long[] elapsed = new Long[1];
			HttpResponseInfo result = DalUtil.doHttp(httpClient, request, dalHttpFactory.createResponseHandler(), elapsed);
			result.elapsedMillis = elapsed[0].longValue();
			logDebug("Elapsed ms="+result.elapsedMillis+" for "+url); //$NON-NLS-1$ //$NON-NLS-2$

			return buildDalResponse(url, result);
		}

		@Override
		public PostBuilder printOn(PrintStream ps) {
			ps.println("Command: "+command); //$NON-NLS-1$
			ps.println("Upload File: " //$NON-NLS-1$
			        +(upload==null ? "null" : upload.getPath())); //$NON-NLS-1$
			int nParameters = postParameters.size();
			ps.println("Parameters: "+nParameters); //$NON-NLS-1$

			String dalCommandUrl = baseUrl + command;

			HttpPostBuilder builder = new HttpPostBuilder(dalHttpFactory, dalCommandUrl) // No logging here !
				.setResponseType(responseType)
				.addParameters(postParameters);

			StringBuilder dataForSignature = new StringBuilder("Data for Signature:\n"); //$NON-NLS-1$
			List<Pair<String,String>> pairs = builder.collectPairsForUpdate(writeToken, dataForSignature);

			ps.println("Pairs for Update: "+pairs.size()); //$NON-NLS-1$
			int count = 0;
			for (Pair<String,String> pair : pairs) {
				ps.println("  " + (++count) //$NON-NLS-1$
				        + ": " +pair.a  //$NON-NLS-1$
				        + "=" + pair.b); //$NON-NLS-1$
				if (count==nParameters) {
					ps.println("---- computed ------"); //$NON-NLS-1$
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
	public PostBuilder prepareExport(String command) {
		return new PostBuilderImpl(command);
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
		return new PostBuilderImpl(command);
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
		logInfo("performUpdate: "+url); //$NON-NLS-1$
		HttpResponseInfo result = DalUtil.doHttp(httpClient, request, dalHttpFactory.createResponseHandler(), elapsed);
		result.elapsedMillis = elapsed[0].longValue();
		logDebug("Elapsed ms=" + result.elapsedMillis + " for " + url); //$NON-NLS-1$ //$NON-NLS-2$

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
		return new PostBuilderImpl(command, upload);
	}

	/**
	 * Perform an upload command for an InputStream using the Fluent programming style.
	 * @param command
	 * @param stream
	 * @return an UpdateBuilder instance
	 */
	@Override
	public UpdateBuilder prepareUpload(String command, Factory<InputStream> stream) {
		return new PostBuilderImpl(command, stream);
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

		logInfo("performUpload: "+url); //$NON-NLS-1$
		Long[] elapsed = new Long[1];
		HttpResponseInfo result = DalUtil.doHttp(httpClient, request, dalHttpFactory.createResponseHandler(), elapsed);
		result.elapsedMillis = elapsed[0].longValue();
		logDebug("Elapsed ms="+result.elapsedMillis+" for "+url); //$NON-NLS-1$ //$NON-NLS-2$

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
	@Override
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

		logInfo("performUpload: "+url); //$NON-NLS-1$
		Long[] elapsed = new Long[1];
		HttpResponseInfo result = DalUtil.doHttp(httpClient, request, dalHttpFactory.createResponseHandler(), elapsed);
		result.elapsedMillis = elapsed[0].longValue();
		logDebug("Elapsed ms="+result.elapsedMillis+" for "+url); //$NON-NLS-1$ //$NON-NLS-2$

		return buildDalResponse(url, result);
	}

}
