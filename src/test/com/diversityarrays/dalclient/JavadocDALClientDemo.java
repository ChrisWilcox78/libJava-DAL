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
package com.diversityarrays.dalclient;

import java.io.IOException;

import com.diversityarrays.dalclient.DALClient;
import com.diversityarrays.dalclient.dalresponse.DalResponse;
import com.diversityarrays.dalclient.dalresponse.DalResponseRecordVisitor;
import com.diversityarrays.dalclient.domain.DalResponseRecord;
import com.diversityarrays.dalclient.domain.ResponseType;
import com.diversityarrays.dalclient.exception.DalLoginException;
import com.diversityarrays.dalclient.exception.DalMissingParameterException;
import com.diversityarrays.dalclient.exception.DalResponseException;
import com.diversityarrays.dalclient.exception.DalResponseFormatException;
import com.diversityarrays.dalclient.impl.CommandBuilder;
import com.diversityarrays.dalclient.impl.DefaultDALClient;

/**
 * An implementation of the code in the javadoc for the package <code>com.diversityarrays.dalclient</code>.
 *
 * @author brian
 *
 */
public class JavadocDALClientDemo {

	public static void main(String[] args) {
		if (args.length < 3) {
			System.err.println("Usage: JavadocDALClientDemo needs 3 args: url, username, password");
			System.exit(1);
		}

		String dalurl   = args[0];
		String username = args[1];
		String password = args[2];

		demo(dalurl, username, password);
	}

	private static void demo(String dalurl, String username, String password) {
		try (final CloseableDALClient client = getDefaultDalClient(dalurl)){
			step1_login(username, password, client);
			
			final Integer[] minMaxGenusId = step2_printAllGenusRecords(client);

			step3_displayFirstFiveMutants(client);

			step4_displayGenusDetails(client, minMaxGenusId);

		} catch (DalResponseException | IOException | DalMissingParameterException e) {
			System.err.println("Query failed: " + e.getMessage());
		}
	}
	
	/**
	 * <b>Step 1:</b> Login and show which group we got switched to.
	 * 
	 * @param username
	 * @param password
	 * @param client
	 */
	private static void step1_login(String username, String password, final DALClient client) {
		System.out.println("=== Step 1 =========");
		try {
			client.login(username, password);
		} catch (DalLoginException | DalResponseException | IOException e) {
			System.err.println("Login failed: " + e.getMessage());
			throw new RuntimeException(e);
		}

		System.out.println("Logged in as id="+client.getUserId()+
				", groupId="+client.getGroupId()+
				", groupName="+client.getGroupName());
		// Output is something like:
		// Logged in as id=7, groupId=2, groupName=users
	}
	
	/**
	 * <b>Step 2:</b> Print out all of the Genus records in the database in JSON format
	 * 
	 * @param client
	 * @return
	 * @throws IOException
	 * @throws DalResponseException
	 * @throws DalResponseFormatException
	 */
	private static Integer[] step2_printAllGenusRecords(final DALClient client)
			throws IOException, DalResponseException, DalResponseFormatException {
		System.out.println("=== Step 2 =========");

		DalResponse genusResponse = client.performQuery("list/genus");
		genusResponse.printOn(System.out);

		// Find the smallest and largest GenusId in the results
		final Integer[] minMaxGenusId = new Integer[2];
		DalResponseRecordVisitor visitor = new DalResponseRecordVisitor() {

			@Override
			public boolean visitResponseRecord(String tagName, DalResponseRecord record) {
				Integer genusId = new Integer(record.rowdata.get("GenusId"));
				if (minMaxGenusId[0] == null || genusId < minMaxGenusId[0]) {
					minMaxGenusId[0] = genusId;
				}
				if (minMaxGenusId[1] == null || genusId > minMaxGenusId[1]) {
					minMaxGenusId[1] = genusId;
				}
				return true; // look at all records
			}
		};
		genusResponse.visitResults(visitor, "Genus");
		System.err.println("GenusIds in range [" + minMaxGenusId[0] 
				+ " to " + minMaxGenusId[1] + "]");
		return minMaxGenusId;
	}
	
	/**
	 * <b>Step 3:</b> We want to display the first 5 GenotypeAlias records
	 * that have a <i>GenotypeAliasName</i> that starts with 'MUTANT'.
	 * We will use the <i>CommandBuilder</i> to replace the parameters in a DAL
	 * operation with specific values.
	 * 
	 * @param client
	 * @throws IOException
	 * @throws DalResponseException
	 * @throws DalMissingParameterException
	 */
	private static void step3_displayFirstFiveMutants(final DALClient client)
			throws IOException, DalResponseException, DalMissingParameterException {
		String cmd;
		try {
			cmd = new CommandBuilder("list/genotypealias/_nperpage/page/_num")
				.setParameter("_nperpage", "5")
				.setParameter("_num",      "1")
				.setFilterClause("GenotypeAliasName LIKE 'MUTANT%'")
				.build();
		} catch (DalMissingParameterException e) {
			throw new RuntimeException(e);
		}

		System.out.println("=== Step 3 =========");
		client.performQuery(cmd).printOn(System.out);

		// Alternatively, you could do it this way:

		DalResponse response = client.prepareQuery("list/genotypealias/_nperpage/page/_num")
				.setParameter("_nperpage", "5")
				.setParameter("_num",      "1")
				.setFilterClause("GenotypeAliasName LIKE 'MUTANT%'")
				.execute();

		response.printOn(System.out);
	}

	/**
	 * <b>Step 4:</b> Display the details for a specific Genus using XML.
	 * This command is so simple we do not need to use <i>CommandBuilder</i>.
	 * 
	 * @param client
	 * @param minMaxGenusId
	 * @throws IOException
	 * @throws DalResponseException
	 * @throws DalResponseFormatException
	 */
	private static void step4_displayGenusDetails(final DALClient client, final Integer[] minMaxGenusId)
			throws IOException, DalResponseException, DalResponseFormatException {
		System.out.println("=== Step 4 =========");
		
		client.setResponseType(ResponseType.XML); // change to XML
		for (Integer id : minMaxGenusId) {
			if (id != null) {
				DalResponse rsp = client.performQuery("get/genus/" + id);
				System.out.println("GenusName#" + id + "="
						+ rsp.getRecordFieldValue("Genus", "GenusName"));
			}
		}
	}

	private static CloseableDALClient getDefaultDalClient(String dalurl) {
		return new DefaultDALClient(dalurl)
		.setAutoSwitchGroupOnLogin(true)     // auto-switch to the first group
		.setResponseType(ResponseType.JSON); // Note: "fluent" coding style is available
	}

}
