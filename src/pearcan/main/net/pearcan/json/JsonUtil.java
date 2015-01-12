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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

public class JsonUtil {
	
    static public boolean DEFAULT_INDENTING = Boolean.getBoolean("net.pearcan.json.INDENTING");
    
	/**
	 * Convert backslashes to slashes.
	 * @param path
	 * @return a String
	 */
    static public String normalizeFileSep(String path) {
        return path.replaceAll("\\\\", "/");
    }
    
    /**
     * Convert backslash, TAB, CR, LF and DQUOTE to escaped form.
     * @param s
     * @return a String
     */
    static public String quoteSafe(Object s) {
        if (s==null)
            return "";
        String tmp = s.toString();
        tmp = tmp.replaceAll("\\\\",  "\\\\\\\\");
        tmp = tmp.replaceAll("\\t", "\\\\t");
        tmp = tmp.replaceAll("\\r", "\\\\r");
        tmp = tmp.replaceAll("\\n", "\\\\n");
        tmp = tmp.replaceAll("\"",  "\\\\\"");
        return tmp;
    }
    
    static public String toJsonString(Iterable<? extends Object> objects) {
    	return toJsonString(objects, DEFAULT_INDENTING);
    }
    
    static public String toJsonString(Iterable<? extends Object> objects, boolean indenting) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            JsonMap.writeArray(baos, objects, indenting);
            baos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baos.toString();
    }
    
    static public String toJsonString(Object[] objects) {
    	return toJsonString(objects, DEFAULT_INDENTING);
    }
    
    static public String toJsonString(Object[] objects, boolean indenting) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            JsonMap.writeArray(baos, objects, indenting);
            baos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baos.toString();
    }
    
    public static String asMapKey(String s) {
    	if (s.indexOf('"')<0) {
            return "\"" + s + "\"";
    	}
    	// Escape all double-quote chars with an escaped version
    	return "\"" + s.replaceAll("\"", "\\\"") + "\"";
    }

	public static String toJsonString(Iterable<? extends Object> keys, Map<? extends Object, ? extends Object> map, boolean indenting) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            JsonMap.writeMap(baos, keys, map, indenting);
            baos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baos.toString();
	}
}
