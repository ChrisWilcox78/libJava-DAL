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

/**
 * Versions:
 * <dl>
 * <dt>5.1.0</dt>
 * <dd>
 * <ul>
 *   <li>Fix incorrect order of name/value pair in {link HttpPostBuilder#buildForUpload} </li>
 *   <li>Use <code>gson</code> library for json parsing</li>
 * </ul>
 * </dd>
 * <dt>5.0.0</dt>
 * <dd>
 * <ul>
 *   <li>
 *     Moved Pair and StringInputStreaam to com.diversityarrays.dalclient.util so that they
 *     don't conflict with other android development libraries.
 *   </li>
 *   <li>
 *     Added <code>wantEmptyRecords</code> to DalResponse with getter/setter.
 *     This change makes <code>DalResponse.visitResults()</code> call the
 *     <code>DalResponseRecordVisitor</code>
 *     with the empty records if the value is set to true.
 *   </li>
 *   <li>
 *     A null or empty filterClause parameter to <code>CommandBuilder.setFilterClause(...)</code>
 *     removes the extant filtering clause from the command being constructed.
 *   </li>
 *   <li>
 *     Introduce <code>OperationKeyword</code> and
 *     <code>addKeywordClause()</code> and <code>addKeywordClauses(...)</code> methods to
 *     <code>CommandBuilder/QueryBuilder</code>
 *   </li>
 *   <li>
 *     Introduce UpdateBuilder as an extension of PostBuilder.
 *   </li>
 * </ul>
 * </dd>
 * <dt>4.0.1</dt>
 * <dd>
 * Empty or null filter in CommandBuilder removes the filter.
 * <p>
 * Add <code>DalResponse.(set|get)WantEmptyRecords</code> with default of <code>false</code>
 * so that empty data records in the DalResponse are not provided to the visitor.
 * </dd>
 * <dt>4.0.0</dt>
 * <dd>
 * Introduce PostBuilder so that query commands can be performed
 * using POST instead of GET.
 * (UpdateBuilder is retained as a synonym).
 * </dd>
 * <dt>3.1.1</dt>
 * <dd>
 * Fix handling of null value for OperationKeyword in CommandBuilder.
 * </dd>
 * <dt>3.1.0</dt>
 * <dd>
 * Introduce <code>QueryBuilder.addKeywordClause()</code> and the enum
 * <code>OperationKeyword</code> to support the other
 * </dd>
 * <dt>3.0.0</dt>
 * <dd>
 * <p>
 * Refactored dependencies on <code>org.apache.http.*</code> into a separate
 * package (<code>com.diversityarrays.dalclient.http</code>) so that the core
 * library can be used on both Android and non-Android environments.
 * <p>
 * For non-Android the package <code>com.diversityarrays.dalclient.httpimpl</code>
 * provides implementations for use with the apache http libraries.
 * </dd>
 *
 * </dl>
 */
public class Main {

	static public final String VERSION = "5.0.0"; //$NON-NLS-1$

	static public void main(String[] args) {
		System.out.println("DAL client library version "+VERSION); //$NON-NLS-1$
		System.exit(0);
	}
}


