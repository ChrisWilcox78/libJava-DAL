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
/**
 * <p>
 * A quick hack at a JSON parser when I needed one.
 * <p>
 * <b>Usage:</b>
 * <pre>
 * JsonParser parser = new JsonParser(a_string_in_json_format);
 * if (parser.isListResult()) {
 *    // has a list of embedded json
 *    List&lt;?&gt; list = parser.getListResult();
 * }
 * else if (parser.isMapResult()) {
 *    JsonMap map = parser.getMapResult();
 * }
 * else {
 *    // eh?
 * }
 * </pre>
 */
package net.pearcan.json;
