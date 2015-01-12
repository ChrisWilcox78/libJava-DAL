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
 * You can request the DAL implementation to work with either XML or JSON.
 * @author brian
 *
 */
public enum ResponseType {
	/**
	 * The client should use XML to interact with the server.
	 */
	XML("xml"),
	
	/**
	 * The client should use JSON to interact with the server.
	 */
	JSON("json"),
	
	/**
	 * This ResponseType is only created for the results of some
	 * server operations (e.g. <code>export/<i>entity</i></code>).
	 * This value is <b>not</b> valid for use in calls to <code>DalClient.setResponseType()</code>.
	 */
	CSV(null),
	;
	
	/**
	 * In case any users want quick access to just the ones which
	 * are valid for using with setResponseType().
	 */
	static public ResponseType[] valuesForPost() {
		return new ResponseType[] { XML, JSON };
	}
	
	public final String postValue;
	ResponseType(String v) {
		this.postValue = v;
	}
	
	public boolean isXML() {
		return this==XML;
	}

	public boolean isJSON() {
		return this==JSON;
	}
}