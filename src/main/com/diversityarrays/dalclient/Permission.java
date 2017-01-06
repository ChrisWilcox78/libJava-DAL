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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * DAL permissions are similar to Unix permission bits.
 * Please refer to the DAL Programmer's Reference for further details.
 * @author brian
 *
 */
public enum Permission {
	LINK(1,  'L'),
	WRITE(2, 'W'),
	READ(4,  'R');
	
	public final int onebit;
	public final char onechar;
	Permission(int onebit, char c) {
		this.onebit = onebit;
		this.onechar = c;
	}
	
	/**
	 * Return a Set of the Permission-s corresponding to the characters in the string.
	 * @param text
	 * @return a Set of Permission
	 */
	static public Set<Permission> createPermissionSet(String text) {
		String uptext = text.toUpperCase();
		Set<Permission> result = new HashSet<Permission>();
		for (Permission p : Permission.values()) {
			if (uptext.indexOf(p.onechar)>=0) {
				result.add(p);
			}
		}
		return result;
	}
	
	/**
	 * Return an integer representation of the specified Permissions.
	 * @param permissions
	 * @return an Integer
	 */
	static public Integer asInteger(String permissions) {
		return asInteger(createPermissionSet(permissions));
	}
	
	/**
	 * Return an integer representation of the specified Permissions.
	 * @param permissions
	 * @return an Integer
	 */
	static public Integer asInteger(Permission ... permissions) {
		int result = 0;
		for (Permission p : permissions) {
			result |= p.onebit;
		}
		return result;
	}

	/**
	 * Return an integer representation of the specified Permissions.
	 * @param permissions
	 * @return an Integer
	 */
	static public Integer asInteger(Collection<Permission> permissions) {
		int result = 0;
		for (Permission p : permissions) {
			result |= p.onebit;
		}
		return result;
	}
}
