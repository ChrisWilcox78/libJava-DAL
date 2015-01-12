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
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonMap {

    private static final String OK = "ok";
	private static final String ERROR = "error";
	private static final byte[] TRUE_BYTES = "true".getBytes();
    private static final byte[] FALSE_BYTES = "false".getBytes();
    private static final byte[] COLON_BYTES = ":".getBytes();
    private static final byte[] NULL_BYTES = "null".getBytes();
    private static final byte[] COMMA_BYTES = ",".getBytes();
    
    private static final byte[] CRLF_BYTES = "\r\n".getBytes();



    static private final String LBRACE = "{";
    static private final byte[] LBRACE_BYTES = LBRACE.getBytes();
    static private final String RBRACE = "}";
    static private final byte[] RBRACE_BYTES = RBRACE.getBytes();
    
    static private final String LBRACKET = "[";
    static private final byte[] LBRACKET_BYTES = LBRACKET.getBytes();
    static private final String RBRACKET = "]";
    static private final byte[] RBRACKET_BYTES = RBRACKET.getBytes();
    
    static private final String DQUOTE = "\"";
    static private final byte[] DQUOTE_BYTES = DQUOTE.getBytes();
    
    private Map<String, Object> valuesMap = new HashMap<String, Object>();
    private List<String> keysInOrder = new ArrayList<String>();
    
//    static private Map<Class<?>, FeaturesGetter> classToFeaturesGetter = new HashMap<Class<?>,FeaturesGetter>();
//    
//	static protected Map<String, Object> getFeatures(Object obj, Class<?> clazz, JsonObject jsonObject) {
//        Map<String, Object> features = new HashMap<String, Object>();
//        
//        FeaturesGetter fg = classToFeaturesGetter.get(clazz);
//        if (fg==null) {
//            try {
//				fg = new FeaturesGetter(clazz, jsonObject.value());
//			} catch (NoSuchFieldException e) {
//				throw new RuntimeException(e);
//			}
//            classToFeaturesGetter.put(clazz, fg);
//        }
//        features = fg.getFeatures(obj);
//        
//        return features;
//    }

    private boolean indenting;
    
    public JsonMap() {
    	this(null, JsonUtil.DEFAULT_INDENTING);
    }
    
    public JsonMap(boolean indent) {
    	this(null, indent);
    }

	public JsonMap(Map<?,?> map) {
    	this(map, JsonUtil.DEFAULT_INDENTING);
    }
    
	public JsonMap(Map<?,?> map, boolean indent) {
    	this.indenting = indent;
    	if (map!=null) {
    		for (Object key : map.keySet()) {
    			put(key.toString(), map.get(key));
    		}
    	}
    }
    
    public List<String> getKeysInOrder() {
    	return Collections.unmodifiableList(keysInOrder);
    }
    
    public Object get(String key) {
    	return valuesMap.get(key);
    }
    
    public JsonMap put(String key, Object v) {
        int pos = keysInOrder.indexOf(key);
        if (pos<0) {
            keysInOrder.add(key);    
        }
        else if (pos>=0 && (pos+1)!=keysInOrder.size()) {
            keysInOrder.remove(pos);
            keysInOrder.add(key);
        }
        
        valuesMap.put(key, v);
        return this;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public JsonMap addToList(String key, Object v) {
        int pos = keysInOrder.indexOf(key);
        if (pos<0) {
            keysInOrder.add(key);    
        }
        else if ((pos+1)!=keysInOrder.size()) {
            keysInOrder.remove(pos);
            keysInOrder.add(key);
        }
        
        List list = null;
        try {
            list = (List) valuesMap.get(key);
        } catch (ClassCastException e) {
        }
        if (list==null) {
            list = new ArrayList();
            valuesMap.put(key, list);
        }
        list.add(v);
        return this;
        
    }
    
    @Override
    public String toString() {
    	return toJsonString(null);
    }

    public String toJsonString() {
    	return toJsonString(null);
    }
    
    public String toJsonString(String prefix) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
        	if (prefix!=null) {
        		baos.write(prefix.getBytes());
        	}
            this.write(baos);
            baos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baos.toString();
    }
    
    public void setIndenting(boolean b) {
        this.indenting = b;
    }
    
    public void write(OutputStream os) throws IOException {
        writeMap(os, keysInOrder, valuesMap, indenting);
    }
    
    // static versions
    
    static protected void writeMap(OutputStream os, Iterable<? extends Object> keys, Map<? extends Object, ? extends Object> map, boolean indenting) throws IOException {
        os.write(LBRACE_BYTES);
        boolean first = true;
        for (Object key : keys) {
            if (first) first = false;
            else {
                os.write(COMMA_BYTES);
                if (indenting) 
                	os.write(CRLF_BYTES);
            }

            Object v = map.get(key);
            os.write(JsonUtil.asMapKey(key.toString()).getBytes());
            os.write(COLON_BYTES);
            writeObject(os, v, indenting);
        }
        os.write(RBRACE_BYTES);
    }

//    static private void writeMap(OutputStream os, String[] keys, Map<? extends Object, ? extends Object> map, boolean indenting) throws IOException {
//        os.write(LBRACE_BYTES);
//        boolean first = true;
//        for (String key : keys) {
//            if (first) first = false;
//            else {
//                os.write(COMMA_BYTES);
//                if (indenting) 
//                	os.write(CRLF_BYTES);
//            }
//
//            Object v = map.get(key);
//            os.write(JsonUtil.asMapKey(key).getBytes());
//            os.write(COLON_BYTES);
//            writeObject(os, v, indenting);
//        }
//        os.write(RBRACE_BYTES);
//    }

    static protected void writeArray(OutputStream os, Iterable<? extends Object> values, boolean indenting) throws IOException {
        boolean first = true;
        os.write(LBRACKET_BYTES);
        for (Object o : values) {
            if (first) first = false;
            else {
                os.write(COMMA_BYTES);
                // if (indenting) os.write(CRLF_BYTES);
            }

            writeObject(os, o, indenting);
        }
        os.write(RBRACKET_BYTES);
    }
    
    static protected void writeArray(OutputStream os, Object[] values, boolean indenting) throws IOException {
        boolean first = true;
        os.write(LBRACKET_BYTES);
        for (Object o : values) {
            if (first) first = false;
            else {
                os.write(COMMA_BYTES);
                // if (indenting) os.write(CRLF_BYTES);
            }
            writeObject(os, o, indenting);
        }
        os.write(RBRACKET_BYTES);
    }
    
	static protected void writeObject(OutputStream os, Object obj, boolean indenting) throws IOException {
        if (obj==null) {
            os.write(NULL_BYTES);
            return;
        }
        
        Class<?> vc = obj.getClass();
        if (vc.isArray()) {
            Object[] values = (Object[]) obj;
            writeArray(os, values, indenting);
        }
//        else if (vc.isAnnotationPresent(JsonObject.class)) {
//            final JsonObject ann = vc.getAnnotation(JsonObject.class);
//            Map<String, Object> features = getFeatures(obj, vc, ann);
//            writeMap(os, ann.value(), features, indenting);
//        }
        else if (obj instanceof JsonMap) {
            ((JsonMap) obj).write(os);
        }
        else if (obj instanceof Map) {
            Map<?,?> map = (Map<?,?>) obj;
            writeMap(os, map.keySet(), map, indenting);
        }
        else if (obj instanceof Collection) {
            writeArray(os, (Collection<?>) obj, indenting);
        }
        else {
            if (obj instanceof Boolean) {
                Boolean b = (Boolean) obj;
                os.write(b.booleanValue() ? TRUE_BYTES : FALSE_BYTES);
            }
//            else if (UnquotedString.class.isAssignableFrom(obj.getClass())) {
//            	UnquotedString us = (UnquotedString) obj;
//            	os.write(us.getUnquotedString().getBytes());
//            }
            else if (Number.class.isAssignableFrom(obj.getClass())) {
            	Number n = (Number) obj;
            	os.write(n.toString().getBytes());
            }
            else {
                String s;
                if (obj instanceof File) {
                	s = JsonUtil.normalizeFileSep(((File) obj).getPath());
                }
                else {
                	s = (obj instanceof String) ? (String) obj : obj.toString();
                }
                
                os.write(DQUOTE_BYTES);
                os.write(JsonUtil.quoteSafe(s).getBytes());
                os.write(DQUOTE_BYTES);
            }
        }
    }

    public static JsonMap create(String key, String value) {
        JsonMap r = new JsonMap();
        r.put(key, value);
        return r;
    }
    
    static public JsonMap makeOkReturn(String text) {
    	JsonMap r = new JsonMap();
    	r.put(OK, text);
    	return r;
    }
    
    static public JsonMap makeErrorReturn(String errmsg) {
    	JsonMap r = new JsonMap();
    	r.put(ERROR, errmsg);
    	return r;
    }
    
    public boolean containsError() {
    	return this.valuesMap.keySet().contains(ERROR);
    }
    
    static public JsonMap makeErrorReturn(Throwable t, boolean showStackTrace) {
        JsonMap r = makeErrorReturn(t.getMessage());
        
        if (t instanceof ParamException) {
        	ParamException pe = (ParamException) t;
        	JsonMap info = pe.getInfo();
        	if (info!=null) {
        		r.put("errinfo", info);
        	}
        }
        
        if (showStackTrace) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            t.printStackTrace(new PrintStream(baos));
            try { baos.close(); } catch (IOException ignore) {}
            
            r.put("stackTrace", baos.toString());
        }
        
        
        return r;
    }
    
    public static JsonMap makeResult(String value) {
        return create(OK, value);
    }




}
