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
import java.util.Map;

import org.apache.commons.collections15.Closure;

import com.diversityarrays.dalclient.util.Pair;

/**
 * This helper interface is used by DALClient to provide a "fluent"
 * style of preparing and executing update or upload commands.
 * @author brian
 * @since 4.0.0
 */
public interface PostBuilder {
	
	PostBuilder addPostParameter(String name, String value);
	PostBuilder addPostParameter(String name, Number value);
	PostBuilder addPostParameters(Map<String,String> postParams);
	
	PostBuilder visitPostParameters(Closure<Pair<String,String>> visitor);

	/**
	 * Perform a DAL query using POST.
	 * @return DalResponse
	 * @throws IOException
	 * @throws DalResponseException
	 * @since 4.0.0
	 */
	DalResponse executeQuery() throws IOException, DalResponseException;

	/**
	 * Synonym for executeUpdate for backwards compatibility.
	 * @return DalResponse
	 * @throws IOException
	 * @throws DalResponseException
	 */
	DalResponse execute() throws IOException, DalResponseException;
	
	/**
	 * Perform a DAL update command using POST.
	 * @return DalResponse
	 * @throws IOException
	 * @throws DalResponseException
	 */
	DalResponse executeUpdate() throws IOException, DalResponseException;
	
	// For debugging
	PostBuilder printOn(PrintStream ps);
}
