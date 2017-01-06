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

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Concrete implementation of DalResponse for XML formatted data.
 * @author brian
 *
 */
public class XmlDalResponse extends AbstractDalResponse {

	public XmlDalResponse(String url, HttpResponseInfo responseInfo) {
		super(url, responseInfo);
	}

	@Override
	public ResponseType getResponseType() {
		return ResponseType.XML;
	}

	/*
	 * We do a pretty printed version - coz we can. 
	 */
	@Override
	public void printOn(PrintStream ps) {
		DalUtil.showXmlResult(getRawResponse(), ps);
	}
	
	/*
	 * We do a pretty printed version - coz we can. 
	 */
	@Override
	public void printOn(PrintWriter pw) {
		DalUtil.showXmlResult(getRawResponse(),pw);
	}
	
	private Document savedXmldoc = null;
	
	private Document getSavedXmldoc() throws DalResponseFormatException  {
		if (savedXmldoc==null) {
			try {
				long elapsed = System.currentTimeMillis();
				String rawResponse = getRawResponse();
				if (DalUtil.looksLikeDoctype(rawResponse)) {
					throw new DalResponseFormatException("response is a DTD"); //$NON-NLS-1$
				}
				savedXmldoc = DalUtil.createXmlDocument(rawResponse);

				if (SHOW_TIMING) {
					elapsed = System.currentTimeMillis() - elapsed;
					System.err.println(this.getClass().getName()+"_parseResult: url="+getUrl()); //$NON-NLS-1$
					System.err.println("\tserver ms="+getHttpResponseInfo().elapsedMillis+"\txml parse ms="+elapsed); //$NON-NLS-1$ //$NON-NLS-2$
				}
			} catch (ParserConfigurationException e) {
				throw new DalResponseFormatException(e);
			} catch (SAXException e) {
				throw new DalResponseFormatException(e);
			} catch (IOException e) {
				throw new DalResponseFormatException(e);
			}
		}
		return savedXmldoc;
	}

	@Override
	public String getRecordFieldValue(String recordName, String fieldName) throws DalResponseFormatException, DalResponseException {
			return DalUtil.getElementAttributeValue(getSavedXmldoc(), recordName, fieldName);
	}

	@Override
	public DalResponseRecord getFirstRecord(String key) throws DalResponseFormatException, DalResponseException {

		DalResponseRecord result = null;
		
		Document xmldoc = getSavedXmldoc();

		String errorMessage = DalUtil.getXmlDalErrorMessage(xmldoc);
		if (errorMessage!=null) {
			throw new DalResponseException(errorMessage);
		}
		
		NodeList nodeList = xmldoc.getElementsByTagName(key);
		if (nodeList.getLength()>0) {
			Node node = nodeList.item(0);
			result = DalUtil.createFrom(getUrl(), node);
		}

		return result!=null ? result : new DalResponseRecord(getUrl(), key);
	}
	
	private List<String> getRecordMetaTagNames(Document xmldoc) throws DalResponseException {
		List<String> result = new ArrayList<String>();

		NodeList recordMetaNodelist = xmldoc.getElementsByTagName(DALClient.TAG_RECORD_META);
		int nRecordMeta = recordMetaNodelist.getLength();
		if (nRecordMeta<=0) {
			throw new DalResponseException("no RecordMeta seen in HTTP response"); //$NON-NLS-1$
		}

		for (int rmi = 0; rmi < nRecordMeta; ++rmi) {
			Node rmNode = recordMetaNodelist.item(rmi);
			NamedNodeMap rmAttributes = rmNode.getAttributes();
			Node tagNameNode = rmAttributes.getNamedItem(DALClient.ATTR_TAG_NAME);
			if (tagNameNode==null) {
				throw new DalResponseException("no RecordMeta/TagName seen in HTTP response ("+rmi+")");  //$NON-NLS-1$//$NON-NLS-2$
			}
			result.add(tagNameNode.getNodeValue());
		}
		
		return result;
	}
	
	@Override
	public boolean visitResults(DalResponseRecordVisitor visitor, Collection<String> wantedTagNames)
	throws DalResponseFormatException, DalResponseException
	{
		Document xmldoc = getSavedXmldoc();

		String errorMessage = DalUtil.getXmlDalErrorMessage(xmldoc);
		if (errorMessage!=null) {
			throw new DalResponseException(errorMessage);
		}
		
		List<String> tagNames;
		if (wantedTagNames==null) {
			tagNames = getRecordMetaTagNames(xmldoc);
		}
		else {
			if (wantedTagNames instanceof List) {
				tagNames = (List<String>) wantedTagNames;
			}
			else {
				tagNames = new ArrayList<String>(wantedTagNames);
			}
		}
		
		return DalUtil.visitXmlResults(getUrl(), xmldoc, tagNames, visitor, getWantEmptyRecords());
	}


	@Override
	public boolean visitResults(DalResponseRecordVisitor visitor, String ... wantedTagNames)
	throws DalResponseFormatException, DalResponseException
	{
		Document xmldoc = getSavedXmldoc();

		String errorMessage = DalUtil.getXmlDalErrorMessage(xmldoc);
		if (errorMessage!=null) {
			throw new DalResponseException(errorMessage);
		}
		
		List<String> tagNames;
		if (wantedTagNames==null || wantedTagNames.length<=0) {
			tagNames = getRecordMetaTagNames(xmldoc);
		}
		else {
			tagNames = Arrays.asList(wantedTagNames);
		}

		return DalUtil.visitXmlResults(getUrl(), xmldoc, tagNames, visitor, getWantEmptyRecords());
	}


	@Override
	public String getResponseErrorMessage() throws DalResponseFormatException {
		return DalUtil.getXmlDalErrorMessage(getSavedXmldoc());
	}

}
