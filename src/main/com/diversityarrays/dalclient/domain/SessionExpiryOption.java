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
package com.diversityarrays.dalclient.domain;

/**
 * Enumerates the valid session expiry options which may be
 * used when logging in to the DAL server.
 * @author brian
 *
 */
public enum SessionExpiryOption {
	/**
	 * The DAL server will allow the session to continue as active until
	 * it is explicitly logged-out.
	 */
	EXPLICIT_LOGOUT("yes", "Explicit Logout"), //$NON-NLS-1$
	/**
	 * The DAL server will cause the session to timeout after a period of inactivity.
	 */
	AUTO_EXPIRE("no", "Auto Expire"); //$NON-NLS-1$
	
	public final String urlValue;
	public final String displayName;

	SessionExpiryOption(String v, String display) {
		this.urlValue = v;
		this.displayName = display;
	}
	
	@Override
	public String toString() {
		return displayName;
	}

	public static SessionExpiryOption lookup(String yesNo) {
		SessionExpiryOption result = null;
		for (SessionExpiryOption seo : values()) {
			if (seo.urlValue.equalsIgnoreCase(yesNo)) {
				result = seo;
				break;
			}
		}
		return result;
	}
}
