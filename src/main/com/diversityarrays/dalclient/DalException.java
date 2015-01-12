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
 * Parent of the tree of all DAL related exceptions in this package.
 * If you find the specific exception clauses too verbose, just catch this one
 * in your catch clause without having to go too far by catching <b>Exception</b>
 * (because that's not good practice).
 * @author brian
 *
 */
public class DalException extends Exception {

	public DalException() {
	}

	public DalException(String message) {
		super(message);
	}

	public DalException(Throwable cause) {
		super(cause);
	}

	public DalException(String message, Throwable cause) {
		super(message, cause);
	}

}
