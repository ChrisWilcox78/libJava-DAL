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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections15.Transformer;

public class Pair<A,B> {
	
	static public <T> Pair<T,T>[] createHomogenousPairs(Collection<T> inputs, final String sep) {
		return createHomogenousPairs(inputs, new Transformer<Pair<T,T>,String>() {
			@Override
			public String transform(Pair<T,T> p) {
				return p.a + sep + p.b;
			}
		});
	}
	
	static public <T> Pair<T,T>[] createHomogenousPairs(Collection<T> inputs, Transformer<Pair<T,T>,String> joiner) {
		List<Pair<T,T>> result = new ArrayList<Pair<T,T>>();
		
		List<T> list = new ArrayList<T>(inputs);
		int n = list.size();
		if (n > 1) {
			for (int a = 0; a < n; ++a) {
				T aa = list.get(a);
				for (int b = a+1; b < n; ++b) {
					T bb = list.get(b);
					
					Pair<T,T> pair = new Pair<T,T>(aa, bb, null);
					String name = joiner.transform(pair);
						
					result.add(new Pair<T,T>(aa, bb, name));
					
					pair = new Pair<T,T>(bb, aa, null);
					name = joiner.transform(pair);
					
					result.add(new Pair<T,T>(bb, aa, name));
				}
			}
		}
		
		@SuppressWarnings("unchecked")
		Pair<T,T>[] pairs = (Pair<T,T>[]) Array.newInstance(Pair.class, result.size());
		pairs = result.toArray(pairs);
		
		return pairs;
	}
	
	public final A a;
	public final B b;
	private final String name;
	
	public Pair(A a, B b) {
		this(a, b, "(" + a+" , "+b+")");
	}
	
	public Pair(A a, B b, String name) {
		this.a = a;
		this.b = b;
		this.name = name;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (! (o instanceof Pair)) return false;
		
		Pair<?,?> other = (Pair<?,?>) o;
		Object oa = other.a;
		Object ob = other.b;
		
		return (oa == null ? a == null : oa.equals(a))
				&&
				(ob == null ? b == null : ob.equals(b));
	}
	
	@Override
	public int hashCode() {
		return (a == null ? 0 : a.hashCode())
			+
				(b == null ? 0 : b.hashCode());
	}
	
	@Override
	public String toString() {
		return name;
	}
	
}
