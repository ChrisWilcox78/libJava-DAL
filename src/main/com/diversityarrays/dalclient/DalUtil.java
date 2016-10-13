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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigInteger;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.DigestInputStream;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Formatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.pearcan.json.JsonMap;
import net.pearcan.json.JsonParser;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.collections15.Predicate;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.diversityarrays.dalclient.http.DalCloseableHttpClient;
import com.diversityarrays.dalclient.http.DalCloseableHttpResponse;
import com.diversityarrays.dalclient.http.DalHeader;
import com.diversityarrays.dalclient.http.DalRequest;
import com.diversityarrays.dalclient.http.DalResponseHandler;

/**
 * Utility routines for use across the rest of the DAL packages.
 * @author brian
 *
 */
public class DalUtil {

	private static final String DIGEST_MD5 = "MD5"; //$NON-NLS-1$

    private static final String ALGORITHM_HMAC_SHA1 = "HmacSHA1"; //$NON-NLS-1$

    public static final String ENCODING_UTF_8 = "UTF-8"; //$NON-NLS-1$

    /**
	 * Get the version number of the DAL Client library.
	 * @return String
	 * @since 2.0
	 */
	static public String getDalClientLibraryVersion() {
		return Main.VERSION;
	}
	
	static public boolean DEBUG = Boolean.getBoolean(DalUtil.class.getName()+".DEBUG"); //$NON-NLS-1$
	
	/**
	 * Use this with SimpleDateFormat for date/time values sent to DAL.
	 */
	static public final String DATE_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss"; //$NON-NLS-1$
	

	/**
	 * This is the beginning of the standard DAL "permission denied" error.
	 * (see getDalErrorMessage())
	 */
	static public final String PERMISSION_DENIED_LOCASE_STEM = "permission denied"; //$NON-NLS-1$

	/**
	 * This is the Charset name used for crypto.
	 */
	static public String cryptCharsetName = ENCODING_UTF_8;
	
	/**
	 * Compare two version number strings and return
	 * -1, 0, 1 if <code>a</code> is respectively less-than, equal to 
	 * or greater-than <code>b</code>.
	 * @param a
	 * @param b
	 * @return -1, 0 or 1
	 */
	static public int compareVersions(String a, String b) {

		Pattern pattern = Pattern.compile("^([0-9]+)"); //$NON-NLS-1$

		StringTokenizer st_a = new StringTokenizer(a, "."); //$NON-NLS-1$
		StringTokenizer st_b = new StringTokenizer(b, "."); //$NON-NLS-1$

		while (st_a.hasMoreTokens() && st_b.hasMoreTokens()) {
			Matcher m = pattern.matcher(st_a.nextToken());
			Integer anum = 0;
			if (m.matches()) {
				anum = new Integer(m.group(1));
			}

			Integer bnum = 0;
			m = pattern.matcher(st_b.nextToken());
			if (m.matches()) {
				bnum = new Integer(m.group(1));
			}

			int result = anum.compareTo(bnum);
			if (result != 0) {
				return result;
			}
		}

		if (st_a.hasMoreTokens()) {
			// well then b doesn't
			return +1;
		}
		if (st_b.hasMoreTokens()) {
			return -1;
		}
		return 0;
	}
	
	/**
	 * Create an SSLContext which will trust all certificates (i.e. it does not validate
	 * any certificate chains).
	 * @return an SSLContext
	 */
	static public SSLContext createTrustingSSLContext() {
		// Create a trust manager that does not validate certificate chains
		final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			@Override
			public void checkClientTrusted( final X509Certificate[] chain, final String authType ) {
			}
			@Override
			public void checkServerTrusted( final X509Certificate[] chain, final String authType ) {
			}
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		} };

		try {
			final SSLContext sslContext = SSLContext.getInstance( "SSL" ); //$NON-NLS-1$

			// Install the all-trusting trust manager
			sslContext.init( null, trustAllCerts, new java.security.SecureRandom() );

			return sslContext;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (KeyManagementException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Convenience interface to URLEncoder.encode(input, "UTF-8").
	 * @param input
	 * @return the encoded String
	 */
	static public String urlEncodeUTF8(String input) {
		String result = input;
		
		try {
			result = URLEncoder.encode(input, ENCODING_UTF_8);
		} catch (UnsupportedEncodingException e) {
		}

		return result;
	}
	
	/**
	 * Convenience interface to URLDecoder.decode(input, "UTF-8").
	 * @param input
	 * @return the decoded String
	 */
	static public String urlDecodeUTF8(String input) {
		String result = input;
		try {
			result = URLDecoder.decode(input, ENCODING_UTF_8);
		} catch (UnsupportedEncodingException e) {
		}
		return result;
	}
	
	/**
	 * Replace all of the parameters in the provided commandTemplate with
	 * the values of the parameters. The commandPattern portions are "/" delimited and 
	 * parameters names are those segments which begin with the letter "_".
	 * <p>
	 * An alternative is to use the CommandBuilder class.
	 * @param prefix prepended to the URL constructed, may be null
	 * @param commandTemplate
	 * @param parameters
	 * @return the URL 
	 * @throws DalMissingParameterException
	 */
	static public String buildCommand(String prefix, String commandTemplate, Map<String,String> parameters)
	throws DalMissingParameterException
	{
		StringBuilder sb = new StringBuilder();
		String sep = prefix == null ? "" : prefix; //$NON-NLS-1$
		for (String p : commandTemplate.split("/")) { //$NON-NLS-1$
			sb.append(sep);
			sep = "/"; //$NON-NLS-1$
			if (p.startsWith("_")) { //$NON-NLS-1$
				String v = parameters.get(p);
				if (v==null) {
					throw new DalMissingParameterException(
					        String.format("Missing value for '%s' in command: %s", p, commandTemplate)); //$NON-NLS-1$
				}
				sb.append(v);
			}
			else {
				sb.append(p);
			}
		}
		
		return sb.toString();
	}
	
	/**
	 * <p>
	 * Execute the provided request using the client and allow the handler to process the response.
	 * Before returning the result from the handler, ensure that the response has been closed.
	 * <p>
	 * This is basically a wrapper around the similarly named method with a Long[] as the last parameter.
	 * @param client
	 * @param request
	 * @param handler
	 * @return the result provided by the handler
	 * @throws IOException
	 */
	static public <T> T doHttp(DalCloseableHttpClient client, DalRequest request, DalResponseHandler<T> handler)
	throws IOException
	{
		return doHttp(client, request, handler, null);
	}
			
	
	/**
	 * Execute the provided request using the client and allow the handler to process the response.
	 * Before returning the result from the handler, ensure that the response has been closed.
	 * If the <code>elapsedTime</code> parameter is supplied then return the number of milliseconds 
	 * spent in the <code>client.execute()</code> method call.
	 * @param client
	 * @param request
	 * @param handler
	 * @param elapsedTimeMillis if non-null it will receive the elapsed time of the actual time spent in the <code>client.execute()</code> method
	 * @return the result provided by the handler
	 * @throws IOException
	 */
	static public <T> T doHttp(DalCloseableHttpClient client, DalRequest request, DalResponseHandler<T> handler, Long[] elapsedTimeMillis)
	throws IOException
	{
		T result = null;

		DalCloseableHttpResponse response = null;
		try {
			if (DEBUG) {
				URI uri = request.getURI();
				System.err.println(DalUtil.class.getSimpleName()+".doHttp: "+uri.toString()); //$NON-NLS-1$
				System.err.println("  --- Headers ---"); //$NON-NLS-1$
				for (DalHeader h : request.getAllHeaders()) {
					System.err.println("\t"+h.getName()+":\t"+h.getValue()); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			long startMillis = System.currentTimeMillis();
			response = client.execute(request);
			if (elapsedTimeMillis!=null) {
				elapsedTimeMillis[0] = System.currentTimeMillis() - startMillis;
			}
			result = handler.handleResponse(response);
		}
		finally {
			if (response!=null) {
				try { response.close(); } catch (IOException ignore) { }
			}
		}
		
		return result;
	}

	/**
	 * Calculate an RFC 2104 compliant HMAC signature.
	 * @param key is the signing key
	 * @param data is the data to be signed 
	 * @return the base64-encoded signature as a String
	 */
	public static String computeHmacSHA1(String key, String data) {
		try {
			byte[] keyBytes = key.getBytes(cryptCharsetName);           
			SecretKeySpec signingKey = new SecretKeySpec(keyBytes, ALGORITHM_HMAC_SHA1);

			Mac mac = Mac.getInstance(ALGORITHM_HMAC_SHA1);
			mac.init(signingKey);

			byte[] rawHmac = mac.doFinal(data.getBytes(cryptCharsetName));

			byte[] hexBytes = new Hex().encode(rawHmac);

			return new String(hexBytes, cryptCharsetName);

		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (InvalidKeyException e) {
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Generate a 64-bit random number.
	 * @return the number as a String
	 */
	static public String createRandomNumberString() {
		SecureRandom random = new SecureRandom();

		byte[] sixtyFourBits = new byte[8];
		random.nextBytes(sixtyFourBits);
		// Let's be positive about this :-)
		sixtyFourBits[0] = (byte) (0x7f & sixtyFourBits[0]);
		
		BigInteger bigint = new BigInteger(sixtyFourBits);
		return bigint.toString();
	}
	
	/**
	 * Computes the MD5 checksum of the bytes in the InputStream.
	 * The input is close()d on exit.
	 * @param input is the InputStream for which to compute the checksum
	 * @return the MD5 checksum as a String of hexadecimal characters
	 */
	static public String computeMD5checksum(InputStream input) {
		DigestInputStream dis = null;
		Formatter formatter = null;
		try {
			MessageDigest md = MessageDigest.getInstance(DIGEST_MD5);
			dis = new DigestInputStream(input, md);
			while (-1 != dis.read())
				;
			
			byte[] digest = md.digest();
			formatter = new Formatter();
			for (byte b : digest) {
				formatter.format("%02x", b); //$NON-NLS-1$
			}
			return formatter.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (dis!=null) {
				try { dis.close(); } catch (IOException ignore) { }
			}
			if (formatter!=null) {
				formatter.close();
			}
		}
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	// JSON tools
	
	
	
	/**
	 * <p>
	 * This interface is used to allow substitution of a different json parser as/if required.
	 * <p>
	 * The input JSON string is expected to be of the form:
	 * <pre>
	 * {
	 *   key-a: ... ,
	 *   key-b: ... ,
	 *   recordName: [
	 *      { attr-1: value-1, attr-2: value-2, fieldName: valueWanted, attr-4: value-4 ... },
	 *      { ... },
	 *      ...
	 *   ],
	 *   ...
	 * }
	 * </pre>
	 */
	static public interface JsonResult {
		List<?> getListAt(String recordName);

		/**
		 * Extract the "Error" message from the JSON response.
		 * Note that if the Message attribute is missing, this will still produce
		 * an error.
		 * <p>
		 * Any other attributes of the <i>Error</i> element will also be returned. This caters
		 * for diagnostic errors which are returned by some of the DAL "update" operations.
		 *
		 * @return the error message or null
		 */
		String getJsonlDalErrorMessage();
		
		/**
		 * Retrieve the value of the key 'fieldName' from the first
		 * array element of the key 'recordName' in this JsonResult.
		 * @param recordName
		 * @param fieldName
		 * @return a String or null
		 */
		String getJsonRecordFieldValue(String recordName, String fieldName);

		/**
		 * Return the first JSON array element from the value of the supplied key.
		 * @param key
		 * @return a Map, an empty one if the key doesn't exist or the array is empty
		 * @throws DalResponseFormatException
		 */
		DalResponseRecord getFirstRecord(String key) throws DalResponseFormatException;

        /**
         * Invoke the <code>visitor.visitResponseRow()</code> method for each result in the response.
         * @param visitor
         * @param wantedTagNames 
         * @return true if all records were visited
         * @throws DalResponseException
         */
        boolean visitResults(DalResponseRecordVisitor visitor, List<String> wantedTagNames) throws DalResponseException;

        /**
         * Invoke the <code>visitor.visitResponseRow()</code> method for each result in the response.
         * @param visitor
         * @param wantedTagNames 
         * @param wantEmptyRecords
         * @return true if all records were visited
         * @throws DalResponseException
         */
        boolean visitResults(DalResponseRecordVisitor visitor, List<String> wantedTagNames, boolean wantEmptyRecords) throws DalResponseException;
	}
	
	/**
	 * This implementation of JsonResult uses my quick json parser hack.
	 */
	static public class JsonMap_JsonResult implements JsonResult {
		
		private final String requestUrl;
		private final JsonMap jsonMap;

		public JsonMap_JsonResult(String requestUrl, JsonMap jsonMap) {
			this.requestUrl = requestUrl;
			this.jsonMap = jsonMap;
		}

		@Override
		public List<?> getListAt(String recordName) {
			List<?> result = null;
			Object object = jsonMap.get(recordName);
			
			if (object instanceof List) {
				result = (List<?>) object;
			}
			return result;
		}
		
		@Override
		public String getJsonRecordFieldValue(String recordName, String fieldName) {
			String result = null;

			List<?> list = getListAt(recordName);
			// we expect a particular structure in a DAL response
			if (list != null) {
				if (! list.isEmpty()) {
					Object resultObj = null;
					Object info = list.get(0);
					if (info instanceof JsonMap) {
						JsonMap jsonMap = (JsonMap) info;
						resultObj = jsonMap.get(fieldName);

					}

					if (resultObj!=null) {
						result = resultObj.toString();
					}
				}
			}

			return result;
		}
		
		@Override
		public String getJsonlDalErrorMessage() {
			String result = null;
			List<?> list = getListAt(DALClient.TAG_ERROR);
			if (list!=null) {
				if (list.isEmpty()) {
					result = "Unknown error: missing element"; //$NON-NLS-1$
				}
				else {
					Object item = list.get(0);
					if (item==null) {
						result = "Unknown error: 'null'"; //$NON-NLS-1$
					}
					else if (item instanceof JsonMap) {
						JsonMap errors = (JsonMap) item;
						StringBuilder sb = new StringBuilder();
						String sep = ""; //$NON-NLS-1$
						for (String key : errors.getKeysInOrder()) {
							sb.append(sep).append(key).append('=').append(errors.get(key));
						}
						result = sb.toString();
					}
					else {
						result = "Unknown error: 'Error' item is "+item.getClass().getName(); //$NON-NLS-1$
					}
				}
			}

			return result;
		}

		@Override
		public DalResponseRecord getFirstRecord(String key) throws DalResponseFormatException {
			DalResponseRecord result = null;
			List<?> list = getListAt(key);
			if (list!=null && ! list.isEmpty()) {
				Object item = list.get(0);
				if (item instanceof JsonMap) {
					result = createFrom(requestUrl, key, (JsonMap) item);
				}
				else {
					throw new DalResponseFormatException(
					        String.format("unexpected type for '%s'[0] : %s", key, item.getClass().getName())); //$NON-NLS-1$
				}
			}
			
			return result == null ? new DalResponseRecord(requestUrl, key) : result;
		}

	    @Override
	    public boolean visitResults(DalResponseRecordVisitor visitor, List<String> wantedTagNames) throws DalResponseException {
	        return visitResults(visitor, wantedTagNames, false);
	    }

		@Override
		public boolean visitResults(DalResponseRecordVisitor visitor, List<String> wantedTagNames, boolean wantEmptyRecords)
		throws DalResponseException 
		{
			boolean result = true;
			
			List<?> rmetaList = getListAt(DALClient.TAG_RECORD_META);
			if (rmetaList==null || rmetaList.isEmpty()) {
				throw new DalResponseException("missing RecordMeta in DAL response"); //$NON-NLS-1$
			}
			
			for (Object rmeta : rmetaList) {
				if (! (rmeta instanceof JsonMap)) {
					throw new DalResponseException("Invalid JSON structure in "+DALClient.TAG_RECORD_META); //$NON-NLS-1$
				}
				JsonMap rmetaMap = (JsonMap) rmeta;
				Object tagnameObj = rmetaMap.get(DALClient.ATTR_TAG_NAME);
				if (tagnameObj==null) {
					throw new DalResponseException("missing RecordMeta/TagName in DAL response"); //$NON-NLS-1$
				}
				
				String tagName = tagnameObj.toString();
				List<?> list = getListAt(tagName);
				if (list==null) {
					throw new DalResponseException(String.format("missing entry for '%s' in DAL response", tagName)); //$NON-NLS-1$
				}
				
				int count = 0;
				for (Object item : list) {
					if (item instanceof JsonMap) {
						JsonMap jsonMap = (JsonMap) item;
						DalResponseRecord record = createFrom(requestUrl, tagName, jsonMap);
						if (wantEmptyRecords || ! record.isEmpty()) {
	                        if (! visitor.visitResponseRecord(tagName, record)) {
	                            result = false;
	                            break;
	                        }
						}
					}
					else {
						throw new DalResponseFormatException(
						        String.format("unexpected type for '%s'[%d] :%s", //$NON-NLS-1$
						                tagName, count, item.getClass().getName()));
					}
					++count;
				}
				
				if (! result) {
					break;
				}
			}
			return result;
		}
	}
	
	/**
	 * Parse the input json string  and return the parse result if it is a Map.
	 * This is because a valid DAL JSON response is always and only of that structure.
	 * @param json
	 * @return either Map or null if the input cannot be parsed
	 */
	public static JsonResult parseJson(String requestUrl, String json) {
		JsonResult result = null;
		try {
			JsonMap jsonMap = new JsonParser(json).getMapResult();
			result = new JsonMap_JsonResult(requestUrl, jsonMap);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	static private DalResponseRecord createFrom(String requestUrl, String tagName, JsonMap input) {
		DalResponseRecord result = new DalResponseRecord(requestUrl, tagName);
		List<String> keys = input.getKeysInOrder();
		if (! keys.isEmpty()) {
			for (String key : keys) {
				Object value = input.get(key);
				if (value==null) {
					result.rowdata.put(key, ""); //$NON-NLS-1$
				}
				else if (value instanceof List) {
						List<?> list = (List<?>) value;
						int count = 0;
						for (Object elem : list) {
							if (elem instanceof JsonMap) {
								JsonMap child = (JsonMap) elem;
								Map<String,String> childMap = asRowdata(child);
								if (childMap!=null) {
									result.addNestedData(key, childMap);
								}
							}
							else {
								result.warnings.add(String.format("unexpected value-type for '%s'[%d] :%s", //$NON-NLS-1$
								        key, count, elem.getClass().getName()));
							}
						}
						++count;
				}
				else if (value instanceof String) {
					result.rowdata.put(key, value.toString());
				}
				else {
					result.warnings.add(String.format("unexpected value-type for '%s' :%s", //$NON-NLS-1$
					        key, value.getClass().getName()));
				}
			}
		}
		
		return result;
	}

	
	/**
	 * Convert the the input into a Map&lt;String,String&gt;.
	 * @param input
	 * @return a Map&lt;String,String&gt;
	 */
	static private Map<String, String> asRowdata(JsonMap input)
	{
		Map<String, String> result = null;
		List<String> keys = input.getKeysInOrder();
		if (! keys.isEmpty()) {
			result = new LinkedHashMap<String,String>();
			for (String key : keys) {
				Object value = input.get(key);
				if (value==null) {
					result.put(key, ""); //$NON-NLS-1$
				}
				else if (value instanceof List) {
					// Hmmm. 
					// TODO array, Collection, JsonMap, Map??
					// ? what can we get from JsonParser?
				}
				else if (value instanceof JsonMap) {
					// Hmmm. this is unexpected
				}
				else {
					result.put(key, value.toString());
				}
			}
		}
		return result;
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	// XML tools
	
	public static Document createXmlDocument(String xml)
	throws ParserConfigurationException, SAXException, IOException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource is = new InputSource(new StringReader(xml));
		Document xmldoc = builder.parse(is);
		return xmldoc;
	}
	public static boolean visitXmlResults(
            String requestUrl, 
            Document xmldoc, 
            List<String> tagNames, 
            DalResponseRecordVisitor visitor) 
    {
	    return visitXmlResults(requestUrl, xmldoc, tagNames, visitor, false);
    }
	
	public static boolean visitXmlResults(
	        String requestUrl, 
	        Document xmldoc, 
	        List<String> tagNames, 
	        DalResponseRecordVisitor visitor,
	        boolean wantEmptyRecords) 
	{
		boolean result = true;
		for (String tagName : tagNames) {
			NodeList resultNodes = xmldoc.getElementsByTagName(tagName);
			int nNodes = resultNodes.getLength();
			for (int ni = 0; ni < nNodes; ++ni) {
				Node node = resultNodes.item(ni);
				DalResponseRecord record = DalUtil.createFrom(requestUrl, node);
				if (record != null && (wantEmptyRecords || ! record.isEmpty())) {
					if (! visitor.visitResponseRecord(tagName, record)) {
						result = false;
						break;
					}
				}
			}

			if (! result) {
				break;
			}
		}
		return result;
	}

	
	public static DalResponseRecord createFrom(String requestUrl, Node node) {
		DalResponseRecord result = new DalResponseRecord(requestUrl, node.getNodeName());
		NamedNodeMap attributes = node.getAttributes();
		if (attributes!=null) {
			int nAttributes = attributes.getLength();
			for (int ai = 0; ai < nAttributes; ++ai) {
				Node attr = attributes.item(ai);
				result.rowdata.put(attr.getNodeName(), attr.getNodeValue());
			}
		}
		
		NodeList childNodes = node.getChildNodes();
		int nChildNodes = childNodes.getLength();
		if (nChildNodes>0) {
			for (int ci = 0; ci < nChildNodes; ++ci) {
				Node child = childNodes.item(ci);
				if (Node.ELEMENT_NODE==child.getNodeType()) {
					String childName = child.getNodeName();
					Map<String,String> childMap = asRowdata(child);
					if (childMap!=null) {
						result.addNestedData(childName, childMap);
					}
				}
			}
		}
		
		return result;
	}
	
	public static Map<String,String> asRowdata(Node node) {
		Map<String,String> result = null;
	
		NamedNodeMap attributes = node.getAttributes();
		if (attributes!=null) {
			int nAttributes = attributes.getLength();
			result = new LinkedHashMap<String,String>(nAttributes);
			for (int ai = 0; ai < nAttributes; ++ai) {
				Node attr = attributes.item(ai);
				result.put(attr.getNodeName(), attr.getNodeValue());
			}
		}
		return result;
	}
	
	public static void showXmlResult(String xml, OutputStream out) {
		if (looksLikeDoctype(xml)) {
			// just print it!
			PrintStream ps = new PrintStream(out);
			ps.print(xml);
			ps.close();
		}
		else {
			try {
				showXmlResult(xml, new OutputStreamWriter(out, ENCODING_UTF_8));
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException("Should never happen!", e); //$NON-NLS-1$
			}
		}
	}

	public static void showXmlResult(String xml, Writer w) {
		if (looksLikeDoctype(xml)) {
			// just print it!
			PrintWriter pw = new PrintWriter(w);
			pw.print(xml);
			pw.close();
		}
		else {
			try {
				writeXmlResult(xml, w);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (TransformerException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void writeXmlViaDocument(String xml, Writer w) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
	        InputSource is = new InputSource(new StringReader(xml));
	        Document xmldoc = builder.parse(is);
	        
	        printXmlDocument(xmldoc, w);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}

	}
	
	public static void writeXmlResult(String xml, Writer w) throws IOException, TransformerException {
		StreamSource source = new StreamSource(new StringReader(xml));

	    TransformerFactory tf = TransformerFactory.newInstance();
	    
	    Transformer transformer = tf.newTransformer();
	    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no"); //$NON-NLS-1$
	    transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
	    transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
	    transformer.setOutputProperty(OutputKeys.ENCODING, ENCODING_UTF_8);
	    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4"); //$NON-NLS-1$ //$NON-NLS-2$

	    transformer.transform(source, new StreamResult(w));
	}
	
	public static void printXmlDocument(Document doc, Writer w) throws IOException, TransformerException {

	    TransformerFactory tf = TransformerFactory.newInstance();
	    
	    Transformer transformer = tf.newTransformer();
	    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no"); //$NON-NLS-1$
	    transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
	    transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
	    transformer.setOutputProperty(OutputKeys.ENCODING, ENCODING_UTF_8);
	    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4"); //$NON-NLS-1$ //$NON-NLS-2$
	    

	    transformer.transform(new DOMSource(doc), 
	         new StreamResult(w));
	}
	
	/**
	 * If the Throwable parameter is an instance of DalResponseHttpException,
	 * extract the error message from it. Otherwise return null.
	 * @param t
	 * @return the String error message from DalResponseHttpException or null
	 */
	static public String extractPossibleDalErrorMessage(Throwable t) {
		String errmsg = null;
		if (t instanceof DalResponseHttpException) {
			DalResponseHttpException he = (DalResponseHttpException) t;
			System.out.println("serverResponse=" + he.responseInfo.serverResponse); //$NON-NLS-1$
			System.out.println("dalErrorMessage='" + he.dalErrorMessage + "'"); //$NON-NLS-1$ //$NON-NLS-2$

			Matcher m = Pattern.compile("^.*Message=(.*)$").matcher(he.dalErrorMessage); //$NON-NLS-1$
			if (m.matches()) {
				System.out.println("Message is '" + m.group(1) + "'"); //$NON-NLS-1$ //$NON-NLS-2$
				errmsg = m.group(1);
			} else {
				errmsg = he.dalErrorMessage;
			}
		}
		return errmsg;
	}
	
	
	/**
	 * <p>
	 * Extract the &lt;Error Message="..." /&gt; from the XML response.
	 * In fact it will retrieve all of the attributes from the 'Error' element
	 * because some DAL responses use the attribute name as part of the message!
	 * <p>
	 * Note that if the Message attribute is missing, this will still produce
	 * an error so it is <b>not</b> the same as just calling:
	 * <code>
	 * getElementAttributeValue(doc, "Error", "Message");
	 * </code>
	 * <br>
	 * Any other attributes of the <i>Error</i> element will also be returned. This caters
	 * for diagnostic errors which are returned by some of the DAL "update" operations.
	 * @param doc
	 * @return error message as a String or null
	 */
	static public String getXmlDalErrorMessage(Document doc) {
		String result = null;
		NodeList errorElements = doc.getElementsByTagName(DALClient.TAG_ERROR);
		if (errorElements.getLength()>0) {
			Node item = errorElements.item(0);
			NamedNodeMap attributes = item.getAttributes();
			if (attributes==null) {
				result = "No attributes available in 'Error' element"; //$NON-NLS-1$
			}
			else {
				// We will get them *all*.
				StringBuilder sb = new StringBuilder();
				String sep = ""; //$NON-NLS-1$
				int nAttributes = attributes.getLength();
				for (int ai = 0; ai < nAttributes; ++ai) {
					Node attr = attributes.item(ai);
					if  (attr != null) {
						sb.append(sep).append(attr.getNodeName()).append('=').append(attr.getTextContent());
						sep = ", "; //$NON-NLS-1$
					}
				}
				result = sb.toString();
			}
		}
		return result;
	}
	
	/**
	 * Return the named attribute from the specified element in the (XML) document.
	 * @param doc
	 * @param tagName
	 * @param attributeName
	 * @return the attribute value or null if either the element or the attribute are missing
	 */
	static public String getElementAttributeValue(Document doc, String tagName, String attributeName) {
		return getAttributeValue(doc.getElementsByTagName(tagName), attributeName);
	}
	
	/**
	 * Return the value of the named attribute from the first Node in the NodeList
	 * or null if the NodeList is empty or the attribute is not present.
	 * @param elements
	 * @param attributeName
	 * @return a String or null
	 */
	static public String getAttributeValue(NodeList elements, String attributeName) {
		String result = null;
		if (elements!=null && elements.getLength()>0) {
			Node item = elements.item(0);
			NamedNodeMap attributes = item.getAttributes();
			if (attributes!=null) {
				Node attr = attributes.getNamedItem(attributeName);
				if (attr!=null) {
					result = attr.getNodeValue();
				}
			}
		}
		return result;
	}
	
	public static List<DalResponseRecord> collectResponseRecords(DalResponse response) throws DalResponseFormatException, DalResponseException {
		return collectResponseRecords(response, null);
	}

	public static List<DalResponseRecord> collectResponseRecords(DalResponse response, final Predicate<String> tagNamePredicate) 
	throws DalResponseFormatException, DalResponseException
	{
		final List<DalResponseRecord> result = new ArrayList<DalResponseRecord>();
		response.visitResults(new DalResponseRecordVisitor() {
			@Override
			public boolean visitResponseRecord(String resultTagName, DalResponseRecord data) {
				if (tagNamePredicate==null || tagNamePredicate.evaluate(resultTagName)) {
					result.add(data);
				}
				return true;
			}
		});
		return result;
	}

	/**
	 * Check if the input appears to be a DOCTYPE response.
	 * @param input
	 * @return true or false
	 */
	public static boolean looksLikeDoctype(String input) {
		// this should handle the "<!ENTITY" form currently seen as well as potentially future "<!DOCTYPE"
		return input!=null && input.startsWith("<!"); //$NON-NLS-1$
	}

	public static boolean isHttpStatusCodeOk(int httpStatusCode) {
		return httpStatusCode >= 200 && httpStatusCode < 300;
	}

	
	static private enum SplitState {
		LOOKING_FOR_SEPARATOR,
		IN_QUOTE,
		LOOKING_FOR_SECOND
		;
	}
	
	static public String[] splitCsvLine(String line, char columnSeparator, char quoteCharacter) {
		return splitCsvLine(line, columnSeparator, quoteCharacter, null);
	}

	static public String[] splitCsvLine(String line, char columnSeparator, char quoteCharacter, String[] headings) {
		
		// Short circuit if no quote characters in the line
		if (line.indexOf(quoteCharacter)<0) {
			return line.split(Pattern.quote(Character.toString(columnSeparator)), -1);
		}

		List<String> result = headings==null ? new ArrayList<String>() : new ArrayList<String>(headings.length);
		int lineLength = line.length();

		StringBuilder field = new StringBuilder();

		SplitState state = SplitState.LOOKING_FOR_SEPARATOR;

		for (int i = 0; i < lineLength; ++i) {
			char ch = line.charAt(i);

			if (state==SplitState.LOOKING_FOR_SEPARATOR) {
				if (ch==columnSeparator) {
					result.add(field.toString());
					field.setLength(0);
				}
				else {
					if (field.length()==0 && ch==quoteCharacter) {
						// Only quote characters after the column separator are recognized
						// as the beginning of a quoted string...
						state = SplitState.IN_QUOTE;
					}
					else {
						field.append(ch);
					}
				}
			}
			else if (state==SplitState.IN_QUOTE) {
				if (ch==quoteCharacter) {
					state = SplitState.LOOKING_FOR_SECOND;
				}
				else {
					field.append(ch);
				}
			}
			else if (state==SplitState.LOOKING_FOR_SECOND) {
				if (ch==quoteCharacter) {
					// Doubled quote - we'll keep it and keep looking...
					field.append(quoteCharacter);
					state = SplitState.IN_QUOTE;
				}
				else if (ch==columnSeparator) {
					// Actually, we've reached the end of the field !
					result.add(field.toString());
					field.setLength(0);

					state = SplitState.LOOKING_FOR_SEPARATOR;
				}
			}
		}
		result.add(field.toString());
		
		return result.toArray(new String[result.size()]);
	}

	static public String join(String sep, Object... parts) {
		StringBuilder sb = new StringBuilder();
		String s = ""; //$NON-NLS-1$
		for (Object o : parts) {
			sb.append(s);
			if (o!=null) {
				sb.append(o);
			}
			s = sep;
		}
		return sb.toString();
	}


	static public String join(String sep, Collection<?> parts) {
		StringBuilder sb = new StringBuilder();
		String s = ""; //$NON-NLS-1$
		for (Object o : parts) {
			sb.append(s);
			if (o!=null) {
				sb.append(o);
			}
			s = sep;
		}
		return sb.toString();
	}

}
