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
package com.diversityarrays.dalclient.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

public class StringInputStream extends InputStream {

    private final StringReader reader;
    
    public StringInputStream(String in) {
        reader = new StringReader(in);
    }
    @Override
    public int read() throws IOException {
        return reader.read();
    }

    @Override
    public long skip(long n) throws IOException {
        return reader.skip(n);
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
    
    @Override
    public synchronized void mark(int readlimit) {
        try {
    	reader.mark(readlimit);
        } catch (IOException e) {
    	throw new RuntimeException(e);
        }
    }
    
    @Override
    public synchronized void reset() throws IOException {
        reader.reset();
    }
    
    @Override
    public boolean markSupported() {
        return reader.markSupported();
    }
}
