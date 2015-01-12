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
import java.util.Map;

import org.apache.commons.collections15.Factory;
import org.apache.commons.logging.Log;

/**
 * Provides an encapsulation of the primary functions required to easily
 * communicate with a DAL server.
 *
 * @author brian
 *
 */
public interface DALClient {

	/**
	 * This keyword is used for specifying filter clauses in the operations of
	 * the form "list/&lt;entity&gt;/_nperpage/page/_num".
	 */
	public static final String FILTERING_KEYWORD = "Filtering";

	/**
	 * This is the tag name for a result record for the DAL operation
	 * <code>login</code>. The value of this constant is <code>"User"</code>.
	 */
	static final String TAG_USER = "User";

	/**
	 * This is the attribute name for the TAG_USER record for the DAL operation
	 * <code>login</code>. The value of this constant is <code>"UserId"</code>.
	 */
	static final String ATTR_USER_ID = "UserId";

	/**
	 * This is the tag name for a result record for the DAL Operation
	 * <code>login</code>. The attribute value contains what is essentially a
	 * session key for use in DAL update operations. The value of this constant
	 * is <code>"WriteToken"</code>.
	 */
	static final String TAG_WRITE_TOKEN = "WriteToken";

	/**
	 * This is the attribute name for a number of DAL operations. The value of
	 * this constant is <code>"Value"</code>.
	 */
	static final String ATTR_VALUE = "Value";

	/**
	 * This is the tag name for a result record for the DAL Operation
	 * <code>get/version</code>. The value of this constant is
	 * <code>"Info"</code>.
	 */
	static final String TAG_INFO = "Info";

	/**
	 * This is the attribute name for the TAG_INFO record for the DAL operation
	 * <code>get/version</code>. The attribute value contains the version number
	 * of the DAL server. The value of this constant is <code>"Version"</code>.
	 */
	static final String ATTR_VERSION = "Version";

	/**
	 * This is an attribute name for the TAG_INFO record for the DAL operation
	 * <code>switch/group</code>. The attribute value contains the name of the
	 * group. The value of this constant is <code>"GroupName"</code>.
	 */
	static final String ATTR_GROUP_NAME = "GroupName";

	/**
	 * This is an attribute name for the TAG_INFO record for the DAL operation
	 * <code>switch/group</code>. The attribute value contains TRUE if the group
	 * is an administrator. The value of this constant is <code>"GAdmin"</code>.
	 */
	static final String ATTR_GADMIN = "GAdmin";

	/**
	 * This is an attribute name for the TAG_INFO record for the DAL operation
	 * <code>get/login/status</code>. The attribute value contains "1" if the
	 * user is logged in and has selected a group (using
	 * <code>switch/group/_gid</code>) and "0" otherwise. The value of this
	 * constant is <code>"GroupSelectionStatus"</code>.
	 */
	static final String ATTR_GROUP_SELECTION_STATUS = "GroupSelectionStatus";

	/**
	 * This is an attribute name for the TAG_INFO record for the DAL operation
	 * <code>get/login/status</code>. The attribute value contains "1" if the
	 * user is logged in and "0" otherwise. The value of this constant is
	 * <code>"LoginStatus"</code>.
	 */
	static final String ATTR_LOGIN_STATUS = "LoginStatus";

	/**
	 * This is the tag name for the record containing the error message returned
	 * by the DAL server. This constant is <code>"Error"</code>.
	 */
	static final String TAG_ERROR = "Error";

	/**
	 * This is the attribute name for the TAG_ERROR record. The attribute value
	 * contains message from the DAL server. The value of this constant is
	 * <code>"Message"</code>.
	 */

	static final String ATTR_MESSAGE = "Message";

	/**
	 * This is the tag name of the result record for most DAL Operations which
	 * return entity records and the value of the attribute ATTR_TAG_NAME
	 * provides the tag name for the individual entity records. The value of
	 * this constant is <code>"RecordMeta"</code>.
	 */
	static final String TAG_RECORD_META = "RecordMeta";

	/**
	 * This is an attribute name for the TAG_RECORD_META record. The value of
	 * this attribute provides the tag name for the response records which
	 * contain the retrieved entity information. The value of this constant is
	 * <code>"TagName"</code>.
	 */
	static final String ATTR_TAG_NAME = "TagName";

	/**
	 * This is the tag name returned by the <code>list/operations</code> DAL
	 * request. The value of this constant is <code>"Operation"</code>.
	 */
	static final String TAG_OPERATION = "Operation";

	/**
	 * This is the attribute name for the operation URL tail returned in the
	 * TAG_OPERATION elements of the results for a <code>list/operations</code>
	 * DAL request. The value of this constant is <code>"REST"</code>.
	 */
	static final String ATTR_REST = "REST";

	/**
	 * This is the name for the meta-data record returned as one of the
	 * <code>list/&lt;entity&gt;/_nperpage/page/_num</code> operations. There
	 * should be an attribute value for ATTR_NUM_OF_PAGES which contains the
	 * number of pages available. The value of this constant is
	 * <code>"Pagination"</code>.
	 */
	static final String TAG_PAGINATION = "Pagination";

	/**
	 * This is an attribute name for the TAG_PAGINATION record. The value of
	 * this attribute provides the number of pages which may be requested. The
	 * value of this constant is <code>"NumOfPages"</code>.
	 */
	static final String ATTR_NUM_OF_RECORDS = "NumOfRecords";

	/**
	 * This is an attribute name for the TAG_PAGINATION record. The value of
	 * this attribute provides the number of pages which may be requested. The
	 * value of this constant is <code>"NumOfPages"</code>.
	 */
	static final String ATTR_NUM_OF_PAGES = "NumOfPages";

	/**
	 * This is an attribute name for the TAG_PAGINATION record. The value of
	 * this attribute provides the page number of the current set of records. It
	 * should reflect the value of the <code>_num</code> parameter supplied in
	 * the request. The value of this constant is <code>"Page"</code>.
	 */
	static final String ATTR_PAGE = "Page";

	/**
	 * This is an attribute name for the TAG_PAGINATION record. The value of
	 * this attribute provides the maximum number records returned in this page
	 * and reflects the value for the <code>_nperpage</code> parameter supplied
	 * in the request. The value of this constant is <code>"NumPerPage"</code>.
	 */

	static final String ATTR_NUM_PER_PAGE = "NumPerPage";

	/**
	 * This is the tag name for the record which contains the id of the newly
	 * created record for DAL <code>add/<i>&lt;entity&gt;</i></code> operations.
	 * The value of the id is in the attribute with the name ATTR_VALUE. This
	 * constant is <code>"ReturnId"</code>.
	 */
	static final String TAG_RETURN_ID = "ReturnId";

	/**
	 * This is an attribute name for the TAG_RETURN_ID record. The value of this
	 * attribute provides the column name of the primary key in the table where
	 * a new record has been created within the <i>kddart</i> database. This
	 * constant is <code>"ParaName"</code>.
	 */
	static final String ATTR_PARA_NAME = "ParaName";

	/**
	 * This is the tag name for the record which contains the URL for the
	 * returned data for DAL <code>upload</code> operations. The value of the
	 * URL is in the attribute with the name ATTR_XML. This constant is
	 * <code>"ReturnIdFile"</code>.
	 */
	static final String TAG_RETURN_ID_FILE = "ReturnIdFile";

	/**
	 * This is an attribute name for the TAG_RETURN_ID_FILE record. The value of
	 * this attribute provides the URL for the uploaded file within the
	 * <i>kddart</i> database. This constant is <code>"ReturnIdFile"</code>.
	 */
	static final String ATTR_XML = "xml";

	/**
	 * Return the base URL for the DAL server.
	 * 
	 * @return the DAL server URL
	 */
	String getBaseUrl();

	/**
	 * Set the type (XML or JSON) to use for responses from server. The default
	 * value is XML.
	 * 
	 * @param responseType
	 * @return this DALClient
	 */
	DALClient setResponseType(ResponseType responseType);

	ResponseType getResponseType();

	/**
	 * If switchGroupOnLogin is true then the client will automatically switch
	 * to the first listed group for the user (if the login is successful).
	 * 
	 * @param switchGroup
	 *            true/false
	 * @return this DALClient to support fluent coding style
	 */
	DALClient setAutoSwitchGroupOnLogin(boolean switchGroup);

	boolean getAutoSwitchGroupOnLogin();

	/**
	 * Set whether sessions need to be explicitly logged out or not.
	 * 
	 * @param sessionExpiryOption
	 */
	void setSessionExpiryOption(SessionExpiryOption sessionExpiryOption);

	SessionExpiryOption getSessionExpiryOption();

	/**
	 * Return whether the client is logged-in or not.
	 * 
	 * @return true/false
	 */
	boolean isLoggedIn();

	/**
	 * Attempt to login using the given credentials.
	 * 
	 * @param username
	 * @param password
	 * @throws IOException
	 * @throws DalLoginException
	 * @throws DalResponseException
	 */
	void login(String username, String password) throws IOException,
			DalLoginException, DalResponseException;

	/**
	 * Logout this client session.
	 */
	void logout();

	/**
	 * Return non-null error message if not successful. When successful, use the
	 * accessor methods of the client to retrieve the groupName and whether the
	 * group has administrator privileges.
	 * 
	 * @param groupId
	 * @return error message as a String
	 * @throws IOException
	 * @throws DalResponseException
	 */
	String switchGroup(String groupId) throws IOException, DalResponseException;

	/**
	 * Return the String representation of the current user id (Integer).
	 * 
	 * @return a String
	 */
	String getUserId();

	/**
	 * Return the current user name.
	 * 
	 * @return a String
	 */
	String getUserName();

	/**
	 * Return the write token provided by DAL on a successful login.
	 * 
	 * @return a String
	 */
	String getWriteToken();

	/**
	 * Return whether the current group has administrator privileges.
	 * 
	 * @return true/false
	 */
	boolean isInAdminGroup();

	/**
	 * Return the current group name.
	 * 
	 * @return a String
	 */
	String getGroupName();

	/**
	 * Return the String representation of the current group id (Integer).
	 * 
	 * @return a String
	 */
	String getGroupId();

	/**
	 * Perform a simple query command.
	 * 
	 * @param command
	 * @return the DalResponse for this query
	 * @throws IOException
	 * @throws DalResponseException
	 */
	DalResponse performQuery(String command) throws IOException,
			DalResponseException;

	/**
	 * Prepare to perform a query command using the Fluent programming style.
	 * For example:
	 * 
	 * <pre>
	 * DalResponse response = client.prepareQuery(&quot;list/genotype/_nperpage/page/_num&quot;)
	 * 		.setParameter(&quot;_nperpage&quot;, 200).setParameter(&quot;_num&quot;, 1).execute();
	 * </pre>
	 * 
	 * @param command
	 * @return the QueryBuilder
	 */
	QueryBuilder prepareQuery(String command);

	// - - - - - - - - - - - - - - - - - - - -

	/**
	 * Perform an UPDATE command with the provided parameters. For example:
	 * 
	 * <pre>
	 * Map&lt;String, String&gt; params = new LinkedHashMap&lt;String, String&gt;();
	 * params.put(&quot;name1&quot;, &quot;value1&quot;);
	 * params.put(&quot;name2&quot;, &quot;value2&quot;);
	 * DalResponse response = client.performUpdate(&quot;update-command&quot;, params);
	 * </pre>
	 * 
	 * @param command
	 * @param postParameters
	 * @return a DalResponse instance
	 * @throws IOException
	 * @throws DalResponseException
	 */
	DalResponse performUpdate(String command, Map<String, String> postParameters)
			throws IOException, DalResponseException;

	/**
	 * Perform an UPDATE command using the Fluent programming style. For
	 * example:
	 * 
	 * <pre>
	 * DalResponse response = client.prepareUpdate("update-command")
	 *    .addPostParameter(name1, value1)
	 *    .addPostParameter(name2, value2)
	 *     :
	 *    .execute();
	 * </pre>
	 * 
	 * @param command
	 * @return an UpdateBuilder instance
	 */
	UpdateBuilder prepareUpdate(String command);

	// - - - - - - - - - - - - - - - - - - - -

	/**
	 * Perform a file upload command with the provided parameters. For example:
	 * 
	 * <pre>
	 * Map&lt;String, String&gt; params = new LinkedHashMap&lt;String, String&gt;();
	 * params.put(&quot;name1&quot;, &quot;value1&quot;);
	 * params.put(&quot;name2&quot;, &quot;value2&quot;);
	 * DalResponse response = client.performUpload(&quot;update-command&quot;, params,
	 * 		fileToUpload);
	 * </pre>
	 * 
	 * @param command
	 * @param postParameters
	 * @param upload
	 * @return a DalResponse instance
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws DalResponseException
	 */
	DalResponse performUpload(String command,
			Map<String, String> postParameters, File upload)
			throws FileNotFoundException, IOException, DalResponseException;

	/**
	 * Perform an upload command for InputStream data with the provided
	 * parameters.
	 * <p>
	 * The factory will be invoked at least twice, once for the data to be sent
	 * and once to compute the checksum on the data.
	 * <p>
	 * For example:
	 * 
	 * <pre>
	 * String content = &quot;&lt;DATA&gt;&lt;/DATA&gt;&quot;;
	 * Factory&lt;InputStream&gt; factory = new Factory&lt;InputStream&gt;() {
	 * 	public InputStream create() {
	 * 		return new com.diversityarrays.util.StringInputStream(content);
	 * 	}
	 * };
	 * Map&lt;String, String&gt; params = new LinkedHashMap&lt;String, String&gt;();
	 * params.put(&quot;name1&quot;, &quot;value1&quot;);
	 * params.put(&quot;name2&quot;, &quot;value2&quot;);
	 * DalResponse response = client.performUpload(&quot;update-command&quot;, params, factory);
	 * </pre>
	 * 
	 * @param command
	 * @param postParameters
	 * @param streamFactory
	 * @return a DalResponse
	 * @throws DalResponseException
	 * @throws IOException
	 */
	DalResponse performUpload(String command,
			Map<String, String> postParameters,
			Factory<InputStream> streamFactory) throws DalResponseException,
			IOException;

	/**
	 * Perform a file upload command using the fluent programming style. For
	 * example:
	 * 
	 * <pre>
	 * DalResponse response = client.prepareUpload("update-command", fileToUpload)
	 *    .addPostParameter(name1, value1)
	 *    .addPostParameter(name2, value2)
	 *     :
	 *    .execute();
	 * </pre>
	 * 
	 * @param command the DAL upload command
	 * @param upload the File to upload
	 * @return a UpdateBuilder instance
	 */
	UpdateBuilder prepareUpload(String command, File upload);

	/**
	 * Perform a file upload command using the fluent programming style. For
	 * example:
	 * 
	 * <pre>
	 * DalResponse response = client.prepareUpload("update-command", streamFactory)
	 *    .addPostParameter(name1, value1)
	 *    .addPostParameter(name2, value2)
	 *     :
	 *    .execute();
	 * </pre>
	 * 
	 * @param command
	 * @param streamFactory
	 * @return a UpdateBuilder instance
	 */
	UpdateBuilder prepareUpload(String command,
			Factory<InputStream> streamFactory);

	/**
	 * Perform an EXPORT command using the Fluent programming style.
	 * <P>
	 * EXPORT commands start with "export/"
	 * <P>
	 * Usage:
	 * 
	 * <pre>
	 * DalResponse response = client.prepareExport(<i>export-command</i>)
	 *    .addPostParameter(name1, value1)
	 *    .addPostParameter(name2, value2)
	 *     :
	 *    .execute();
	 * </pre>
	 * 
	 * @param command
	 * @return an UpdateBuilder instance
	 */
	UpdateBuilder prepareExport(String command);

	/**
	 * Perform an EXPORT command with the provided parameters. This is a
	 * convenience method which calls performUpdate(...);
	 * <P>
	 * EXPORT commands start with "export/".
	 * <P>
	 * Usage:
	 * <pre>
	 * Map&lt;String, String&gt; params = new LinkedHashMap&lt;String, String&gt;();
	 * params.put(&quot;name1&quot;, &quot;value1&quot;);
	 * params.put(&quot;name2&quot;, &quot;value2&quot;);
	 * DalResponse response = client.performExport(&quot;export/genotype&quot;, params);
	 * </pre>
	 * 
	 * @param command
	 * @param postParameters
	 * @return a DalResponse instance
	 * @throws IOException
	 * @throws DalResponseException
	 */
	DalResponse performExport(String command, Map<String, String> postParameters)
			throws IOException, DalResponseException;

	/**
	 * Establish a log for the client.
	 * 
	 * @param log
	 */
	void setLog(Log log);

}
