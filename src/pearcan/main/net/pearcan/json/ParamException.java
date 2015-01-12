/*
 * Utility routines for use in Java programs - extracted from pearcan-lib
 * Copyright (C) 2015  Brian Pearce
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
package net.pearcan.json;

public class ParamException extends Exception {

	private JsonMap info;
	public ParamException() {
	}

	public ParamException(String message) {
		super(message);
	}

	public ParamException(Throwable cause) {
		super(cause);
	}

	public ParamException(String message, Throwable cause) {
		super(message, cause);
	}

	public ParamException(String message, JsonMap info) {
		this(message, null, info);
	}

	public ParamException(String message, Throwable cause, JsonMap info) {
		super(message, cause);
		this.info = info;
	}

	public JsonMap getInfo() {
		return info;
	}
}
