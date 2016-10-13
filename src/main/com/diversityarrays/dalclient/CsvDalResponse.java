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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is a specialised DalResponse to cater for the results from an operation like "export/genotype".
 * 
 * The visitResults() methods can operate in two modes:
 * <ul>
 *   <li>"raw" mode: each call to visitResults is supplied with a single element in 
 *   the DalResponseRecord's rowdata field with a key of "csvline" (constant=FIELD_CSVLINE).
 *   The tagname associated with this is "csvdata" (constant=TAGNAME_CSVDATA).
 *   </li>
 *   <li>
 *   If, however, you call <code>setUseHeadings(true)</code>, the visitResults() methods
 *   will treat the first result line as a headings line and use these as the keys of the rowdata
 *   field inthe DalResponseRecord.
 *   </li>
 * </ul>
 * 
 * Example:
 * <pre>
 *    CsvDalResponse r = (CsvDalResponse) client.performQuery("export/genotype");
 *    r.setUseHeadings(true);
 *    r.visitResults(new DalResponseRecordVisitor() {
 *        int lineNumber = 0;
 *        public boolean visitResponseRecord(String resultTagName, DalResponseRecord record) {
 *            ++lineNumber;
 *            System.out.println(resultTagName+"#"+lineNumber);
 *            for (String key : record.rowdata.keySet()) {
 *                System.out.println("  "+key+": "+record.rowdata.get(key));
 *            }
 *            return true;
 *        }
 *    });
 * </pre>
 * @author brian
 *
 */
public class CsvDalResponse extends AbstractDalResponse {

	public static final String FIELD_CSVLINE = "csvline"; //$NON-NLS-1$
	/**
	 * This is a dummy tagname to allow visitResults() to give something back to the visitor.
	 */
	public static final String TAGNAME_CSVDATA = "csvdata"; //$NON-NLS-1$
	
	private boolean useHeadings;
	private String[] headings;
	
	private String headingsPrefix = "#"; //$NON-NLS-1$
	private char quoteCharacter = '"';
	private char columnSeparator = ',';

	private String firstResponseLine;

	public CsvDalResponse(String url, HttpResponseInfo responseInfo) {
		super(url, responseInfo);
	}
	
	public String getHeadingsPrefix() {
		return headingsPrefix;
	}

	public void setHeadingsPrefix(String headingsPrefix) {
		this.headingsPrefix = headingsPrefix;
	}

	public void setQuoteCharacter(char ch) {
		quoteCharacter = ch;
	}
	
	public char getQuoteCharacter() {
		return quoteCharacter;
	}
	
	public void setColumnSeparator(char ch) {
		this.columnSeparator = ch;
	}
	
	public char getColumnSeparator() {
		return columnSeparator;
	}
	
	public void setUseHeadings(boolean b) {
		useHeadings = b;
	}
	
	public boolean getUseHeadings() {
		return useHeadings;
	}
	
	public String[] getCsvHeadings() {
		if (headings==null) {
			headings = getCsvHeadingsInternal(getFirstResponseLine());
		}
		return headings;
	}
	
	private String[] getCsvHeadingsInternal(String line) {
		if (headings==null) {
			if (line==null) {
				line = getFirstResponseLine();
			}
			
			if (headingsPrefix != null && ! headingsPrefix.isEmpty()) {
				Pattern p = Pattern.compile("^"+Pattern.quote(headingsPrefix)+"(.*)$"); //$NON-NLS-1$ //$NON-NLS-2$
				Matcher m = p.matcher(line);
				if (m.matches()) {
					line = m.group(1);
				}
			}

			headings = DalUtil.splitCsvLine(line, columnSeparator, quoteCharacter, null);
		}
		return headings;
	}

	@Override
	public ResponseType getResponseType() {
		return ResponseType.CSV;
	}

	@Override
	public String getResponseErrorMessage() throws DalResponseFormatException {
		// There can be an error for a CSV response
		return null;
	}

	@Override
	public boolean visitResults(DalResponseRecordVisitor visitor,
			Collection<String> wantedTagNames)
	throws DalResponseFormatException, DalResponseException {

		boolean result = true;
		
		if (wantedTagNames==null || wantedTagNames.contains(TAGNAME_CSVDATA)) {
			result = visitCsvLines(visitor);
		}
		
		return result;
	}

	@Override
	public boolean visitResults(DalResponseRecordVisitor visitor, String... wantedTagNames)
	throws DalResponseFormatException, DalResponseException {
		boolean result = true;
		
		boolean go = true;
		if (wantedTagNames!=null && wantedTagNames.length>0) {
			go = false;
			for (String s : wantedTagNames) {
				if (TAGNAME_CSVDATA.equals(s)) {
					go = true;
					break;
				}
			}
		}
		
		if (go) {
			result = visitCsvLines(visitor);
		}
		
		return result;
	}

	private boolean visitCsvLines(DalResponseRecordVisitor visitor) throws DalResponseException {
		
		boolean result = true;
		
		String url = getUrl();
		
		BufferedReader br = null;
		try {
			br = new BufferedReader(new StringReader(getRawResponse()));
			String line;
			int lnum = 0;
			int nHeadings = 0;
			while (null != (line = br.readLine())) {
				++lnum;
				if (useHeadings && lnum==1) {
					if  (headings==null) {
						getCsvHeadingsInternal(line);
					}
					nHeadings = headings.length;
				}
				else {
					DalResponseRecord rr = new DalResponseRecord(url, TAGNAME_CSVDATA);
					
					if (useHeadings) {
						String[] fields = DalUtil.splitCsvLine(line, columnSeparator, quoteCharacter, headings);
						int nFields = fields.length;
						
						int maxidx = Math.max(nFields, headings.length);
						for (int idx = 0; idx < maxidx; ++idx) {
							String h = (idx < nHeadings) ? headings[idx] : "column-"+idx; //$NON-NLS-1$
							String v = (idx < nFields) ? fields[idx] : null;
							rr.rowdata.put(h, v);
						}
					}
					else {
						rr.rowdata.put(FIELD_CSVLINE, line);
					}
					
					if (! visitor.visitResponseRecord(TAGNAME_CSVDATA, rr)) {
						result = false;
						break;
					}
				}
			}
		} catch (IOException e) {
			throw new DalResponseException(e);
		}
		finally {
			if (br!=null) {
				try { br.close(); }
				catch (IOException ignore) {}
			}
		}
		return result;
	}
	


	@Override
	public DalResponseRecord getFirstRecord(String recordName)
	throws DalResponseFormatException, DalResponseException {
		DalResponseRecord result = null;
		if (TAGNAME_CSVDATA.equals(recordName)) {
			result = new DalResponseRecord(getUrl(), TAGNAME_CSVDATA);
			result.rowdata.put(FIELD_CSVLINE, getFirstResponseLine());
		}
		return result;
	}

	@Override
	public String getRecordFieldValue(String recordName, String fieldName)
	throws DalResponseFormatException, DalResponseException {
		String result = null;
		
		if (TAGNAME_CSVDATA.equals(recordName)) {
			if (useHeadings) {
				
			}
			else if (FIELD_CSVLINE.equals(fieldName)) {
				result = getFirstResponseLine();
			}
		}
		return result;
	}
	
	private String getFirstResponseLine() {
		if (firstResponseLine==null) {
			firstResponseLine = getResponseLine(1);
		}
		return firstResponseLine;
	}
	
	private String getResponseLine(int wanted) {
		String result = null;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new StringReader(getRawResponse()));
			String line;
			int lnum = 0;
			while (null != (line = br.readLine())) {
				if (++lnum==wanted) {
					result = line;
					break;
				}
			}
		} catch (IOException ignore) {
		}
		finally {
			if (br!=null) {
				try { br.close(); }
				catch (IOException ignore) {}
			}
		}
		
		return result;
	}

}
