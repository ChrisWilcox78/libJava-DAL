/*
 * Utility routines for use in Java programs - extracted from pearcan-lib
 * Copyright (C) 2015  Brian Pearce
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
package net.pearcan.json;

import java.io.PrintStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class JsonParser {

	private void printList(PrintStream ps, List<Object> list, int indent) {
		ps.print("[ ");
		String sep = "";
		for (Object obj : list) {
			ps.print(sep);
			printObj(ps, obj, indent);
			sep = " , ";
		}
		ps.println(" ]");
	}
	
	private void printMap(PrintStream ps, JsonMap map, int indent) {
		ps.print("{ ");
		String sep = "";
		for (String key : map.getKeysInOrder()) {
			ps.print(sep);
			ps.print(key);
			ps.print(" : ");
			Object obj = map.get(key);
			printObj(ps, obj, indent);
			ps.println();
			sep = " , ";
		}
		ps.println(" }");
	}

	@SuppressWarnings("unchecked")
	private void printObj(PrintStream ps, Object obj, int indent) {
		if (obj instanceof JsonMap) {
			printMap(ps, (JsonMap) obj, indent+1);
		}
		else if (obj instanceof List) {
			printList(ps, (List<Object>) obj, indent+1);
		}
		else {
			ps.print(obj.toString());
		}
	}
	


	public void printOn(PrintStream ps) {
		if (isListResult()) {
			printList(ps, listResult, 0);
		}
		else if (isMapResult()) {
			printMap(ps, mapResult, 0);
		}
		else {
			ps.println("Null");
		}
	}

	enum TokenType {
		L_BRACE("L_BRACE"), R_BRACE("R_BRACE"), 
		COLON("COLON"), COMMA("COMMA"), 
		L_BRACKET("L_BRACK"), R_BRACKET("R_BRACK"), 
		UQSTRING("string"), QSTRING("string"), 
		BACKSLASH("BACKSLASH"),
		EOF("eof");
		
		public String s;

		TokenType(String s) {
			this.s = s;
		}
		
		public boolean isString() {
			return QSTRING==this || UQSTRING==this;
		}
	}
	
	class Token {
		public final TokenType ttype;
		public final String stringValue;
		public final Object value;
		
		Token(TokenType tt, String s) {
			this(tt, s, s);
		}
		
		Token(TokenType tt, String s, Object v) {
			this.ttype = tt;
			this.stringValue = s;
			this.value = v;
		}
		
		public String toString() {
			return ttype.s+"="+value;
		}
	}
	
	private final Token EOF_TOKEN = new Token(TokenType.EOF, null, null);

	private StringTokenizer st;
	private int tokenNumber = 0;
	private boolean eof = false;
	private boolean debug;
	
	private List<Object> listResult = null;
	private JsonMap      mapResult  = null;

	private final String inputString;
	
	public JsonParser(String data) throws ParseException {
		this(data, false);
	}
	
	public JsonParser(String data, boolean dbg) throws ParseException {
		this.debug = dbg;
		this.inputString = data;
		st = new StringTokenizer(inputString, " \t\n\r\f{}[]:,\"\'\\", true);
		parse();
	}
	
	
	
	public List<Object> getListResult() {
		return listResult;
	}

	public boolean isListResult() {
		return listResult!=null;
	}


	public JsonMap getMapResult() {
		return mapResult;
	}

	public boolean isMapResult() {
		return mapResult != null;
	}


	public Token nextToken(String from) throws ParseException {
		Token result = null;
		if (eof) {
			result = EOF_TOKEN;
		}
		else {
			
			while (result==null && st.hasMoreTokens()) {
				
				String s = st.nextToken();
				++tokenNumber;
				
				if ("{".equals(s)) {
					result = new Token(TokenType.L_BRACE, s);
				}
				else if ("}".equals(s)) {
					result = new Token(TokenType.R_BRACE, s);
				}
				else if ("[".equals(s)) {
					result = new Token(TokenType.L_BRACKET, s);
				}
				else if ("]".equals(s)) {
					result = new Token(TokenType.R_BRACKET, s);
				}
				else if ("\"".equals(s) || "'".equals(s)) {
					String q = s;
					StringBuilder sb = new StringBuilder();
					while (st.hasMoreTokens()) {
						String s2 = st.nextToken();
						if ("\\".equals(s2)) {
							if (! st.hasMoreTokens()) {
								break;
							}
							s2 = st.nextToken();
						}
						else if (q.equals(s2)) {
							break;
						}
						sb.append(s2);
					}
					result = new Token(TokenType.QSTRING, sb.toString());
				}
				else if (":".equals(s)) {
					result = new Token(TokenType.COLON, s);
				}
				else if (",".equals(s)) {
					result = new Token(TokenType.COMMA, s);
				}
				else if ("\\".equals(s)) {
					result = new Token(TokenType.BACKSLASH, s);
				}
				else {
					// stand-alone token
					if (s.replaceAll("\\s", "").trim().length()<=0) {
						// whitespace
					}
					else {
						result = new Token(TokenType.UQSTRING, s);
					}
				}
			}
			
			if (result==null) {
				result = EOF_TOKEN;
			}
			eof = TokenType.EOF == result.ttype;
		}

		
		if (debug) {
			System.out.println(from+"\tToken["+tokenNumber+"] : "+result);
		}
		return result;
	}
	
	private void parse() throws ParseException {

		Token token = nextToken("priming");
		if (TokenType.L_BRACE == token.ttype) {
			mapResult = parseMap();
		}
		else if (TokenType.L_BRACKET == token.ttype) {
			listResult = parseList();
		}
		else {
			throw new ParseException("Expected { or [ but got "+token, tokenNumber);
		}
	}
	
	private List<Object> parseList() throws ParseException {
		List<Object> result = new ArrayList<Object>();
		
		Token token = null;
		boolean lookingForComma = false;
		while (EOF_TOKEN != (token = nextToken("parseList"))) {
			if (lookingForComma) {
				if (TokenType.R_BRACKET == token.ttype) {
					return result;
				}
				if (TokenType.COMMA != token.ttype) {
					throw new ParseException("Looking for COMMA but got "+token, tokenNumber);
				}
				lookingForComma = false;
			}
			else {
				if (TokenType.L_BRACE == token.ttype) {
					JsonMap map = parseMap();
					result.add(map);
				}
				else if (TokenType.L_BRACKET == token.ttype) {
					List<Object> list = parseList();
					result.add(list);
				}
				else if (TokenType.QSTRING == token.ttype) {
					result.add(token.value);
				}
				else if (TokenType.UQSTRING == token.ttype) {
					if ("null".equals(token.value)) {
						result.add(null);
					}
					else {
						result.add(token.value);
					}
				}
				else if (TokenType.R_BRACKET == token.ttype) {
					// empty !
					return result;
				}
				else {
					throw new ParseException("Expected a string or number but got "+token, tokenNumber);
				}
				
				lookingForComma = true;
			}
		}
		return null;
	}

	private JsonMap parseMap() throws ParseException {
		JsonMap result = new JsonMap();

		Token token;
		String key = null;
		boolean lookingForColon = false;
		boolean lookingForComma = false;
		
		while (EOF_TOKEN != (token = nextToken("parseMap"))) {
			if (key==null) {
				if (token.ttype == TokenType.R_BRACE) {
					return result;
				}
		
				if (lookingForComma) {
					if (TokenType.COMMA != token.ttype) {
						throw new ParseException("Expected a COMMA but got "+token, tokenNumber);
					}
					lookingForComma = false;
				}

				else {
					if (! token.ttype.isString()) {
						throw new ParseException("Expected a map-key (string or number) but got "+token, tokenNumber);
					}
					if (TokenType.UQSTRING==token.ttype && "null".equals(token.value)) {
						throw new ParseException("'null' is invalid as a key", tokenNumber);
					}
					key = token.stringValue;
					lookingForColon = true;
				}
			}
			else if (lookingForColon) {
				if (TokenType.UQSTRING == token.ttype && "".equals(token.stringValue.replaceAll("\\s+", ""))) {
					// ignore whitespace
				}
				else {
					if (TokenType.COLON != token.ttype) {
						throw new ParseException("Expecting COLON but got "+token, tokenNumber);
					}
					lookingForColon = false;
				}
			}
			else {
				
				if (TokenType.L_BRACE == token.ttype) {
					JsonMap map = parseMap();
					result.put(key, map);
				}
				else if (TokenType.L_BRACKET == token.ttype) {
					List<Object> list = parseList();
					result.put(key, list);
				}
				else {
					if (! token.ttype.isString()) {
						throw new ParseException("Expected a string (or number) but got "+token, tokenNumber);
					}
					
					if (TokenType.UQSTRING == token.ttype && "null".equals(token.value)) {
						result.put(key, null);
					}
					else {
						result.put(key, token.value);
					}
				}
				
				key = null;
				lookingForComma = true;
			}
		}
		
		throw new ParseException("EOS found while parsing map", tokenNumber);
	}
}
