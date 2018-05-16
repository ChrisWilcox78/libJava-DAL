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
import java.util.Map;

import com.diversityarrays.dalclient.dalresponse.DalResponse;
import com.diversityarrays.dalclient.domain.OperationKeyword;
import com.diversityarrays.dalclient.exception.DalMissingParameterException;
import com.diversityarrays.dalclient.exception.DalResponseException;

public interface QueryBuilder {

	/**
	 * Set the value of the named parameter.
	 * @param name
	 * @param value
	 * @return this QueryBuilder
	 */
	public QueryBuilder setParameter(String name, String value);

	/**
	 * Set the value of the named parameter.
	 * @param name
	 * @param value
	 * @return this QueryBuilder
	 */
	public QueryBuilder setParameter(String name, Number value);

	/**
	 * Set the value of a number of parameters.
	 * @param params
	 * @return this QueryBuilder
	 */
	public QueryBuilder setParameters(Map<String, String> params);

	/**
	 * Set the prefix for this command which will be used by the build() method.
	 * @param prefix
	 * @return this QueryBuilder
	 */
	public QueryBuilder setPrefix(String prefix);

	/**
	 * Set the filter clause which will returned after the template components
	 * and URL-encoded. This is the same as using <code>addKeywordClause(OperationKeyword.FILTERING, filter)</code>.
	 * @param filter
	 * @return this QueryBuilder
	 */
	public QueryBuilder setFilterClause(String filter);

	/**
	 * Add a value for the given OperationKeyword if the clause is a non-empty String.
	 * A null String is treated as empty.
	 * @param keyword 
	 * @param clause String 
	 * @return this QueryBuilder
	 */
	public QueryBuilder addKeywordClause(OperationKeyword keyword, String clause);
	
	/**
	 * Add all of the clauses.
	 * @param clauses a possibly null or empty Map
	 * @return this QueryBuilder
	 */
	public QueryBuilder addKeywordClauses(Map<OperationKeyword,String> clauses);

	/**
	 * Construct and return the DAL operation complete with Filtering clause if requested.
	 * @return a String
	 * @throws DalMissingParameterException
	 */
	public String build() throws DalMissingParameterException;

	/**
	 * Execute the query using the originally supplied DAL client.
	 * @return the response from DAL
	 * @throws IOException
	 * @throws DalResponseException
	 * @throws DalMissingParameterException
	 */
	public DalResponse execute() throws IOException, DalResponseException, DalMissingParameterException;

}
