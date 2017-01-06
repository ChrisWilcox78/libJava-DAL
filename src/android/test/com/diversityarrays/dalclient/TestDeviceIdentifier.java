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
package com.diversityarrays.dalclient;

import java.util.UUID;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdManager.DiscoveryListener;
import android.net.nsd.NsdManager.ResolveListener;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import com.diversityarrays.util.AndroidDeviceId;

public class TestDeviceIdentifier {

	public static void main(String[] args) {
		for (String prefix : new String[] { "Brian", "35", Long.toString(System.currentTimeMillis()) } ) {
			UUID uuid = AndroidDeviceId.getDeviceUUID(prefix);
			System.out.println(prefix+"\t"+uuid);
		}

	}

	
	static class ServiceDiscovery {
		
		protected static final String TAG = ServiceDiscovery.class.getName();
		
		private DiscoveryListener discoveryListener;

		private final String serviceType;

		protected String serviceNameToFind;

		private NsdManager nsdManager;

		protected NsdServiceInfo service;

		protected ResolveListener resolveListener = new ResolveListener() {
			
			@Override
			public void onServiceResolved(NsdServiceInfo serviceInfo) {
				Log.i(TAG, "Service Resolved: "+serviceInfo);
				
				service = serviceInfo;
				Log.i(TAG, "Service is at "+service.getHost()+":"+service.getPort());
			}
			
			@Override
			public void onResolveFailed(NsdServiceInfo service, int code) {
				Log.e(TAG, "Resolve Failed: "+service+" code="+code);
			}
		};
		
		ServiceDiscovery(Context context, String stype) {
			serviceType = stype;
			
			nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
		}

		public void start() {
			
			discoveryListener = new NsdManager.DiscoveryListener() {
				
				@Override
				public void onDiscoveryStarted(String regType) {
					Log.d(TAG, "Service discovery started");
				}
				
				@Override
				public void onStartDiscoveryFailed(String serviceType, int code) {
					Log.e(TAG, "Start Discovery failed: code="+code);
					nsdManager.stopServiceDiscovery(this);
				}
				
				@Override
				public void onServiceFound(NsdServiceInfo service) {
					Log.d(TAG, "Service found: "+service);
					if (! service.getServiceType().equals(serviceType)) {
						Log.d(TAG, "Unknown Service Type: "+service.getServiceType());
					}
					else if (service.getServiceName().equals(serviceNameToFind)) {
						nsdManager.resolveService(service, resolveListener);
					}
					
				}
				
				@Override
				public void onServiceLost(NsdServiceInfo service) {
					Log.e(TAG, "Service Lost: "+service);
				}
				
				@Override
				public void onDiscoveryStopped(String serviceType) {
					Log.i(TAG, "Discovery stopped: "+serviceType);
				}
				
				@Override
				public void onStopDiscoveryFailed(String serviceType, int code) {
					Log.e(TAG, "Stop Discovery failed: code="+code);
					nsdManager.stopServiceDiscovery(this);
				}

			};
		}
	}
}
