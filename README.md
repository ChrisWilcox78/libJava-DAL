# libJava-DAL
DAL client library for Java

This library lets you code client applications to use a DAL server and
not be concerned with the low-level 

---

	DALClient client = new DefaultDALClient(dalurl)
	    .setAutoSwitchGroupOnLogin(true)     // auto-switch to the first group
	    .setResponseType(ResponseType.JSON); // Note: "fluent" coding style is available

	try {

// <b>Step 1:</b> Login and show which group we got switched to.

	    System.out.println("=== Step 1 =========");
	    try {
	        client.login(username, password);
	    } catch (DalLoginException e) {
	        System.err.println("Login failed: "+e.getMessage());
	        return;
	    } catch (DalResponseException e) {
	        System.err.println("Login failed: "+e.getMessage());
	        return;
	    } catch (IOException e) {
	        System.err.println("Login failed: "+e.getMessage());
	        return;
	    }

	    System.out.println("Logged in as id="+client.getUserId()+
	        	       ", groupId="+client.getGroupId()+
	        	       ", groupName="+client.getGroupName());

// Output is something like:<br>
// `Logged in as id=7, groupId=2, groupName=users`

// <b>Step 2:</b> Print out all of the Genus records in the database in JSON format

	    System.out.println("=== Step 2 =========");

	    DalResponse genusResponse = client.performQuery("list/genus");
	    genusResponse.printOn(System.out);

// <b>Step 3:</b> We want to display the first 5 GenotypeAlias records
<br>
// that have a <i>GenotypeAliasName</i> that starts with 'MUTANT'.
<br>
// In this variation, we use a <i>CommandBuilder</i> to replace the parameters in a DAL
<br>
// operation with specific values.

	    String cmd;
	    try {
	        cmd = new CommandBuilder("list/genotypealias/_nperpage/page/_num")
	            .setParameter("_nperpage", "5")
	            .setParameter("_num",      "1")
	            .setFilterClause("GenotypeAliasName LIKE 'MUTANT'") // this is optional
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
	        .setFilterClause("GenotypeAliasName LIKE 'MUTANT'") // this is optional
	        .execute();

		response.printOn(System.out);

// <b>Step 4:</b> Display the details for a specific Genus using XML.
<br>
// This command is so simple we do not need to use <i>CommandBuilder</i>.

	    System.out.println("=== Step 4 =========");
	    client.setResponseType(ResponseType.XML); // change to XML
	    client.performQuery("get/genus/" + 2).printOn(System.out);

	} catch (DalResponseException e) {
	    System.err.println("Query failed: "+e.getMessage());
	} catch (IOException e) {
	    System.err.println("Query failed: "+e.getMessage());
	} catch (DalMissingParameterException e) {
	    System.err.println("Query failed: "+e.getMessage());
	} finally {
	    // Make sure that we finish off the session.
	    // If we didn't get logged in, this is a NO-OP.
	    client.logout();   // Note that DefaultDALClient.finalize() also does this
	}
---
