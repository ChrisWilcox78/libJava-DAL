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
 * Versions:
 * <dl>
 * 
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
	
	static public final String VERSION = "3.0.0";

	static public void main(String[] args) {
		System.out.println("DAL client library version "+VERSION);
		System.exit(0);
	}
}


