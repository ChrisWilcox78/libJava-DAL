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
package sample;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.diversityarrays.dalclient.DALClient;
import com.diversityarrays.dalclient.DalLoginException;
import com.diversityarrays.dalclient.DalResponse;
import com.diversityarrays.dalclient.DalResponseException;
import com.diversityarrays.dalclient.DalResponseFormatException;
import com.diversityarrays.dalclient.DalResponseRecord;
import com.diversityarrays.dalclient.DalResponseRecordVisitor;
import com.diversityarrays.dalclient.DefaultDALClient;

public class SimpleDemo {

	public static void main(String[] args) {
		if (args.length != 3) {
			System.err.println("?Need 3 args: URL USERNAME PASSWORD");
			System.exit(1);
		}
		
		SimpleDemo simpleDemo = null;
		try {
			simpleDemo = new SimpleDemo(args[0], args[1], args[2]);
			
			String genusId = simpleDemo.addGenus("SimpleDemoGenus");
			
			System.out.println(" - - - - - - - - - - ");
			simpleDemo.listGenus(System.out);
			System.out.println(" - - - - - - - - - - ");
			
			if (genusId != null) {
				System.out.println("Created Genus id=" + genusId);

				try {	
					System.out.println("Adding a single genotype to GenusId=" + genusId);
					String genotypeId = simpleDemo.addGenotype(genusId);
					if (genotypeId != null) {
						System.out.println("\tDeleting the single genotype just added");
						DalResponse rsp = simpleDemo.client.performUpdate("delete/genotype/" + genotypeId, null);
						rsp.printOn(System.out);
					}
					
					System.out.println("Adding a multiple genotypes to GenusId=" + genusId);
					Map<String, List<String>> genotypeIdsByGenusId = simpleDemo.addBulkGenotype(genusId);
					if (! genotypeIdsByGenusId.isEmpty()) {
						for (List<String> genotypeIds : genotypeIdsByGenusId.values()) {
							simpleDemo.deleteGenotypes(genotypeIds);
						}
					}
				}
				finally {
					System.out.println("Deleting Genus id=" + genusId);
					simpleDemo.deleteGenus(genusId);
				}
			}
			
		} catch (DalLoginException | DalResponseException | IOException e) {
			System.err.println(e.getMessage());
		} finally {
			if (simpleDemo != null) {
				simpleDemo.finish();
			}
		}

	}
	
	public void deleteGenotypes(List<String> genotypeIds) {
		for (String id : genotypeIds) {
			try {
				addUpdateDelete_record("delete/genotype/" + id, null);
				System.out.println("Deleted GenotypeId=" + id);
			} catch (DalResponseException | IOException e) {
				System.err.println("Unable to delete GenotypeId=" + id);
			}
		}
	}

	private void listGenus(final PrintStream out) throws DalResponseException, IOException {
		DalResponse rsp = client.performQuery("list/genus");
		DalResponseRecordVisitor visitor = new DalResponseRecordVisitor() {
			int sequence = 0;
			@Override
			public boolean visitResponseRecord(String resultTagName, DalResponseRecord record) {
				++sequence;
				out.println("==== " + resultTagName + "#" + sequence + " ====");
				for (String key : record.rowdata.keySet()) {
					out.println(key + ":\t" + record.rowdata.get(key));
				}
				return true;
			}
		};
		rsp.visitResults(visitor, "Genus");
	}

	DALClient client;
	
	/**
	 * This constructor performs the same function as the entire Auth.java example
	 * @param dalUrl
	 * @param username
	 * @param password
	 * @throws DalLoginException
	 * @throws DalResponseException
	 * @throws IOException
	 */
	public SimpleDemo(String dalUrl, String username, String password) 
	throws DalLoginException, DalResponseException, IOException 
	{
		client = new DefaultDALClient(dalUrl);
		client.login(username, password);
		DalResponse listGroupResponse = client.performQuery("list/group");
//		listGroupResponse.printOn(System.out);
		String groupId = listGroupResponse.getRecordFieldValue("SystemGroup", "SystemGroupId");
		client.switchGroup(groupId);
		System.out.println("Switched to group#" + client.getGroupId() + "=" + client.getGroupName());
	}
	
	public void deleteGenus(String id) throws DalResponseException, IOException {
		DalResponse rsp = client.performUpdate("delete/genus/" + id, null);
		rsp.printOn(System.out);
	}
	
	/**
	 * Add a new genus with the given name and return the ID assigned by DAL
	 * @param genusName
	 * @return
	 * @throws DalResponseException
	 * @throws IOException
	 */
	public String addGenus(String genusName) throws DalResponseException, IOException {
		
		DalResponse response = client.prepareUpdate("add/genus")
			.addPostParameter("GenusName", genusName)
			.execute();
		
		response.printOn(System.out);
		
		String result = null;
		if (response.getResponseErrorMessage()==null) {
			result = response.getRecordFieldValue(DALClient.TAG_RETURN_ID, DALClient.ATTR_VALUE);
		}
		
		return result;
	}
	
	/**
	 * Add a single Genotype to the given Genus.
	 * @param genusId
	 * @return
	 * @throws DalResponseException
	 * @throws IOException
	 */
	public String addGenotype(String genusId) throws DalResponseException, IOException {

		String genotypeName = "GenoTest " + System.currentTimeMillis();

		try {
			// discover what we need to supply in postParameters
			// client.performQuery("genotype/list/field").printOn(System.out);
		
			DalResponse response = client.prepareUpdate("add/genotype")
					.addPostParameter("GenotypeName",       genotypeName)
					.addPostParameter("GenusId",            genusId)
					.addPostParameter("SpeciesName",        "Testing")
//					.addPostParameter("GenotypeAcronym",    "") // optional
					.addPostParameter("OriginId",           "0")
					.addPostParameter("CanPublishGenotype", "0")
					.addPostParameter("GenotypeNote",       "Testing from Java")
					.addPostParameter("GenotypeColor",      "N/A")
					.addPostParameter("OwnGroupPerm",       "7")
					.addPostParameter("AccessGroupId",      "0")
					.addPostParameter("AccessGroupPerm",    "5")
					.addPostParameter("OtherPerm",          "5")
					.execute();

			response.printOn(System.out);
			
			String result = null;
			String errmsg = response.getResponseErrorMessage();
			if (errmsg==null) {
				result = response.getRecordFieldValue(DALClient.TAG_RETURN_ID, DALClient.ATTR_VALUE);
			}
			else {
				System.err.println("?Unable to create Genotype '" + genotypeName + "' : " + errmsg);
			}

			return result;
		} catch (DalResponseException e) {
			System.err.println("?Unable to create Genotype '" + genotypeName + "' : " + e.getMessage());
			throw e;
		} catch (IOException e) {
			throw e;
		}
	}
	
	public Map<String, List<String>> addBulkGenotype(String genusId) throws IOException, DalResponseException {
		Map<String,String> columnIndexByHeading = new HashMap<String, String>();
		
		File uploadFile = createUploadFile(genusId, columnIndexByHeading);
		
		DalResponse importResponse = client.prepareUpload("import/genotype/csv", uploadFile).addPostParameters(columnIndexByHeading).execute();
		importResponse.printOn(System.out);
		
		// The IDs of the Genotype records that were created are available in an XML resource of the form:
		// <DATA>
		//   <ReturnId GenusId="25" GenotypeId="12124" GenotypeName="..." />
		//   <ReturnId GenusId="25" GenotypeId="12125" GenotypeName="..." />
		//   <ReturnId GenusId="25" GenotypeId="12126" GenotypeName="..." />
		//   <ReturnId GenusId="25" GenotypeId="12127" GenotypeName="..." />
		//   <ReturnId GenusId="25" GenotypeId="12128" GenotypeName="..." />
		// </DATA>
		
		// The URL of that resource is provided in the response for the "import" command.
		// We will now collect thos GenotypeId values so that our caller can delete them and
		// clean up the database after this test.
		
		return collectCreatedGenotypeIds(importResponse);
	}

	private Map<String, List<String>> collectCreatedGenotypeIds(
			DalResponse importResponse) throws DalResponseFormatException,
			DalResponseException, IOException 
	{
		final Map<String,List<String>> result = new HashMap<String, List<String>>();

		String xmlResponseUrl = importResponse.getRecordFieldValue(DALClient.TAG_RETURN_ID_FILE, DALClient.ATTR_XML);
		
		if (xmlResponseUrl != null) {
			
			DalResponse response = client.performQuery(xmlResponseUrl);
			
			DalResponseRecordVisitor visitor = new DalResponseRecordVisitor() {
				@Override
				public boolean visitResponseRecord(String resultTagName, DalResponseRecord record) {
					String genusId = record.rowdata.get("GenusId");
					List<String> list = result.get(genusId);
					if (list == null) {
						list = new ArrayList<String>();
						result.put(genusId, list);
					}
					list.add(record.rowdata.get("GenotypeId"));
					return true;
				}
			};
			
			response.visitResults(visitor, DALClient.TAG_RETURN_ID);
		}
		
		return result;
	}
	
	private File createUploadFile(String genusId, Map<String,String> columnIndexByHeading) throws IOException {
		
		Set<String> expectedColumnHeadings = new HashSet<String>();
		Collections.addAll(expectedColumnHeadings, 
				"GenotypeName,GenusId,SpeciesName,GenotypeAcronym,OriginId,CanPublishGenotype,GenotypeColor,GenotypeNote".split(","));
		
		Map<String,String> replacements = new HashMap<String, String>();
		replacements.put("{RandomNum}", Long.toString(System.currentTimeMillis()));
		replacements.put("{GenusId}", genusId);
		
		File result = File.createTempFile("geno", ".csv");
		
		PrintStream out = null;
		try {
			out = new PrintStream(result);
			for (String line : BULK_GENOTYPE_CSV_LINES) {
				if (columnIndexByHeading.isEmpty()) {
					if (line.trim().isEmpty()) {
						throw new RuntimeException("No headings in input");
					}
					String[] headings = line.split(",");
					for (int i = 0; i < headings.length; ++i) {
						String hdg = headings[i];
						if (! expectedColumnHeadings.contains(hdg)) {
							throw new RuntimeException("Invalid heading '" + hdg + "' in input");
						}
						columnIndexByHeading.put(hdg, Integer.toString(i));
					}
				}
				else {
					String outline = replaceTokensInTemplate(line, replacements);
					out.println(outline);
				}
			}
		}
		finally {
			if (out != null) {
				out.close();
			}
		}
		
		result.deleteOnExit();
		return result;
	}
	
	// This would be better done using an RE
	private String replaceTokensInTemplate(String template, Map<String,String> replacements) {
		String rest = template;
		
		boolean any = true;
		while (any) {
			
			for (String key : replacements.keySet()) {
				any = false;
				String value = replacements.get(key);
			
				StringBuilder sb = new StringBuilder();
				int pos;
				int spos = 0;
				while (-1 != (pos = rest.indexOf(key, spos))) {
					any = true;
					sb.append(rest.substring(spos, pos));
					sb.append(value);
					spos = pos + key.length();
				}
				if (any) {
					sb.append(rest.substring(spos));
					rest = sb.toString();
				}
			}
		}
		
		return rest;
	}
	/**
	 * 
	 * @param cmd
	 * @param params
	 * @return
	 * @throws DalResponseException
	 * @throws IOException
	 
	// This is the equivalent of the add_record, update_record and delete_record example methods
	// in the FunctionsDal.java example
	*/
	public boolean addUpdateDelete_record(String cmd, Map<String,String> params) 
	throws DalResponseException, IOException 
	{
		DalResponse response = client.performUpdate(cmd, params);
		
		String error = response.getResponseErrorMessage();
		if (error == null) {
			return true;
		}
		
		System.err.println("?" + cmd + " ERROR=" + error);
		return false;
	}
	
	
	public boolean add_record_upload(String cmd, Map<String, String> params, String filePath) 
	throws FileNotFoundException, DalResponseException, IOException 
	{
		DalResponse response = client.performUpload(cmd, params, new File(filePath));
		
		String error = response.getResponseErrorMessage();
		if (error == null) {
			return true;
		}
		
		System.err.println("?" + cmd + " ERROR=" + error);
		return false;
	}
	
	
	private void finish() {
		if (client != null) {
			client.logout();
		}
	}
	
	
	static private final String[] BULK_GENOTYPE_CSV_LINES = {
		"GenotypeName,GenusId,SpeciesName,GenotypeAcronym,OriginId,CanPublishGenotype,GenotypeColor,GenotypeNote",
		"\"Francis Phobe_{RandomNum}\",{GenusId},\"Not sure\",\"\",0,0,\"White\",\"Perfumed- Perfumed -This rose produces short to medium stems and has exhibition style flowers. Great for picking and quick to repeat. The perfect white if you want a nice white for picking.\"",
		"\"Fresh Cream_{RandomNum}\",{GenusId},\"Not sure\",\"\",0,0,\"White\",\"Perfumed - The large cream buds are produced on long stems. A pointed shaped bud but will sometimes throw the odd rounded shaped bud to. This variety is increasing its popularity every year as it always looks so good in our showgardens.\"",
		"\"Friendship_{RandomNum}\",{GenusId},\"Not sure\",\"\",0,0,\"Red\",\"Perfumed - A beautiful long stemmed, highly perfumed mid pink rose. The buds are large and blowsy and open very flat. A very easy rose to grow\"",
		"\"Fruitee_{RandomNum}\",{GenusId},\"Not sure\",\"\",0,0,\"Yellow/red\",\"A vibrant little rose of yellow and red markings. A great variety for standard roses as it has short stocky stems. It produces loads of flowers and is quick to repeat.\"",
		"\"Full Sail_{RandomNum}\",{GenusId},\"Not sure\",\"\",0,0,\"White\",\"Perfumed -One of our most popular white hybrid teas. This is the white sport from the well known rose Aotearoa. It has a very strong perfume and produces loads of flowers on a very healthy plant. Very disease resistant, a highly recommended variety.\"",
	};
}
