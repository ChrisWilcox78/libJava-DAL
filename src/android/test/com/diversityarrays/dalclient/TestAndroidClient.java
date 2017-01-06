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

import java.io.IOException;

import com.diversityarrays.dalclient.DALClient;
import com.diversityarrays.dalclient.DalLoginException;
import com.diversityarrays.dalclient.DalResponseException;
import com.diversityarrays.dalclient.DefaultDALClient;

public class TestAndroidClient {

	public static void main(String[] args) {
		
		System.setProperty(DefaultDALClient.class.getName()+".HTTP_FACTORY_CLASS_NAME", 
				"com.diversityarrays.dalclient.httpandroid.AndroidDalHttpFactory");
		DALClient client = new DefaultDALClient("http://kddart-t.diversityarrays.com/dal");
		client.setAutoSwitchGroupOnLogin(true);
		
		try {
			client.login("admin", "kdd@rt");
			
			System.out.println("groupName="+client.getGroupName());
		} catch (DalLoginException | DalResponseException | IOException e) {
			e.printStackTrace();
		} finally {
			client.logout();
		}
	}

}
