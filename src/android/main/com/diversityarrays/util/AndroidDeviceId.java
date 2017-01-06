/*
 * dalclient library - provides utilities to assist in using KDDart-DAL servers
 * Copyright (C) 2015,2016,2017  Diversity Arrays Technology
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
package com.diversityarrays.util;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import android.os.Build;

/**
 * Provides a mechanism to construct a unique device identifier.
 * Note: this code adapted from something I found while browsing but I can't remember where now!
 * @author brian
 */
public class AndroidDeviceId {
	

	static private final String SERIAL = "SERIAL";
	
	static private final Set<String> WANTED_FIELDS;
	static {
		Set<String> set = new HashSet<String>();
		// These are the "device id" fields
		Collections.addAll(set, "BOARD,BRAND,CPU_ABI,DEVICE,MANUFACTURER,PRODUCT".split(","));
		set.add(SERIAL);
		WANTED_FIELDS = Collections.unmodifiableSet(set);
	}

	// No instances allowed
	private AndroidDeviceId() {
	}
	
	static public UUID getDeviceUUID(String prefix) {
		String serial = null;
		
		// Build the device id string
		StringBuilder sb = new StringBuilder(prefix);
		for (Field f : Build.class.getDeclaredFields()) {
			if (String.class == f.getType()) {
				if (WANTED_FIELDS.contains(f.getName())) {
					f.setAccessible(true);
					try {
						Object obj = f.get(null);
						String value = (obj==null) ? null : obj.toString();
						
						if (SERIAL.equals(f.getName())) {
							serial = value;
						}
						else if (value!=null) {
							sb.append(":").append(value);
						}
					} catch (IllegalArgumentException ignore) {
					} catch (IllegalAccessException ignore) {
					}
				}
			}
		}
		
		String deviceId = sb.toString();
		
		if (serial==null) {
			serial = "dummy";
		}

		return new UUID(deviceId.hashCode(), serial.hashCode());
	}
}
