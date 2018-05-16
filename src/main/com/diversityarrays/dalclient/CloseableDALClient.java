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

import java.io.Closeable;

import com.diversityarrays.dalclient.domain.ResponseType;

public interface CloseableDALClient extends DALClient, Closeable {

	/**
	 * Set the type (XML or JSON) to use for responses from server. The default
	 * value is XML.
	 *
	 * @param responseType
	 * @return this CloseableDALClient
	 */
	CloseableDALClient setResponseType(ResponseType responseType);
	
	/**
	 * If switchGroupOnLogin is true then the client will automatically switch
	 * to the first listed group for the user (if the login is successful).
	 *
	 * @param switchGroup
	 *            true/false
	 * @return this CloseableDALClient to support fluent coding style
	 */
	CloseableDALClient setAutoSwitchGroupOnLogin(boolean switchGroup);
}
