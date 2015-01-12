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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Provide a mechanism to build DAL command urls using templates which are "/" delimited
 * and where the parameter segments begin with the "_" (underscore) character.
 * <p>
 * Usage:
 * <pre>
 * String command = new CommandBuilder("doSomething/<b>_p1</b>/part/<b>_suffix</b>/after-suffix")
 *   .setParameter("_p1", "P1-VALUE")
 *   .setParameter("_suffix", "SUFFIX_VALUE")
 *   .setPrefix("this-comes-first##")
 *   .build();
 * </pre>
 * The above returns the value:<pre>
 * <code>this-comes-first##doSomething/<b>P1-VALUE</b>/part/<b>SUFFIX_VALUE</b>/after-suffix</code>
 * </pre>
 * @author brian
 *
 */
public class CommandBuilder implements QueryBuilder {

	private static final String ENCODING_CHARSETNAME = "UTF-8";

	private String prefix;
	private String commandPattern;
	private Map<String,String> parameters = new HashMap<String, String>();
	private String filterClause;
	private final DALClient dalClient;

	/**
	 * Construct a new CommandBuilder using the specified template.
	 * Templates are "/" delimited paths with parameters being those path
	 * components which start with the underscore (_) character.
	 * @param commandTemplate
	 */
	public CommandBuilder(String commandTemplate) {
		this(commandTemplate, null);
	}

	public CommandBuilder(String commandTemplate, DALClient client) {
		this.commandPattern = commandTemplate;
		this.dalClient = client;
	}
	

	@Override
	public QueryBuilder setParameter(String name, String value) {
		parameters.put(name, value);
		return this;
	}
	
	@Override
	public QueryBuilder setParameter(String name, Number value) {
		parameters.put(name, value.toString());
		return this;
	}
	
	@Override
	public QueryBuilder setParameters(Map<String,String> params) {
		if (params!=null) {
			for (Map.Entry<String,String> e : params.entrySet()) {
				parameters.put(e.getKey(), e.getValue());
			}
		}
		return this;
	}
	
	@Override
	public QueryBuilder setPrefix(String prefix) {
		this.prefix = prefix;
		return this;
	}
	
	@Override
	public QueryBuilder setFilterClause(String filter) {
		this.filterClause = filter;
		return this;
	}
	
	@Override
	public String build() throws DalMissingParameterException {
		StringBuilder sb = new StringBuilder();
		String sep = prefix == null ? "" : prefix;
		for (String p : commandPattern.split("/")) {
			sb.append(sep);
			sep = "/";
			if (p.startsWith("_")) {
				String v = parameters.get(p);
				if (v==null) {
					throw new DalMissingParameterException("Missing value for '"+p+"' in command: "+commandPattern);
				}
				sb.append(v);
			}
			else {
				sb.append(p);
			}
		}
		
		if (filterClause!=null && ! filterClause.isEmpty()) {
			try {
				// TODO 1) check with Puthick about whether Filtering can occur in the PATH of a POST
				// TODO 2) Use (new URI(filterClause).getPath()) instead of URLEncoder.encode(filterClause)
				sb.append("?").append(DALClient.FILTERING_KEYWORD).append("=").append(URLEncoder.encode(filterClause, ENCODING_CHARSETNAME));
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}
		
		return sb.toString();
	}

	@Override
	public DalResponse execute() throws IOException, DalResponseException, DalMissingParameterException {
		if (dalClient==null) {
			throw new DalMissingParameterException("DALClient was not supplied in constructor");
		}
		String cmd = build();
		return dalClient.performQuery(cmd);
	}

}