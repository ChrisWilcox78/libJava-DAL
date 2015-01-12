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


/**
 * <p>
 * Provide an implementation of this when you wish to process one or more of the records
 * returned by a DAL command. You can short-circuit this processing by returning false
 * when you do not wish to process any further records.
 * </p>
 * <p>
 * An alternative is to use the DalResponseRecords class.
 * </p>
 * @author brian
 * @since 2.0
 */
public interface DalResponseRecordVisitor {
	/**
	 * Called for each result row.
	 * @param resultTagName
	 * @param record
	 * @return false to stop any more calls
	 */
	public boolean visitResponseRecord(String resultTagName, DalResponseRecord record);
}
