/*
 * dalclient library - provides utilities to assist in using KDDart-DAL servers
 * Copyright (C) 2015  Diversity Arrays Technology
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
/**
 * <p>
 * Provides support for developing Java programs to interact with a DAL server.
 * <p>
 * For example:
 * <pre>
 * // Create the client and tell it to receive JSON data from the DAL server.
 * DALClient client = new DefaultDALClient("https://server/dal")
 *     .setSwitchGroupOnLogin(true)        // auto-switch to the first group
 *     .setResponseType(ResponseType.JSON) // Note: "fluent" coding style is available
 * try {
 *     // <b>Step 1:</b> Login and show which group we got switched to.
 *     
 *     client.login(username, password);
 *     System.out.println("Logged in as id="+client.getUserId()+
 *                        ", groupId="+client.getGroupId()+
 *                        ", groupName="+client.getGroupName());
 *     // Output is something like:
 *     // Logged in as id=7, groupId=2, groupName=users
 * 
 *     // <b>Step 2:</b> Print out all of the ItemUnit records in the database in JSON format
 *
 *     DalResponse itemUnitResponse = client.performQuery("list/itemunit");
 *     itemUnitResponse.printOn(System.out);
 *     
 *     // <b>Step 3:</b> We want to display the first 20 TrialUnit records
 *     //         which have a barcode beginning with the character '7'.
 *     // We will use the <b>CommandBuilder</b> to replace the parameters in a DAL
 *     // operation with specific values.
 *
 *     String cmd = new CommandBuilder("list/trialunit/<b>_nperpage</b>/page/<b>_num</b>")
 *                    .setParameter("_nperpage", "20")
 *                    .setParameter("_num",      "1")
 *                    .setFilterClause("TrialUnitBarcode LIKE '7%'") // this is optional
 *                    .build();	
 *     client.performQuery(cmd).printOn(System.out);
 *     
 *     // Alternatively:
 *     DalResponse page1 = client.performQuery(cmd);
 *     
 *     String numOfPages = pageResponse.getRecordFieldValue( DALClient.TAG_PAGINATION, DALClient.ATTR_NUM_OF_PAGES );
 *     String pageNumber = pageResponse.getRecordFieldValue( DALClient.TAG_PAGINATION, DALClient.ATTR_PAGE );
 *     
 *     System.out.println("There are " + nPages + " pages of data available");
 *     System.out.println("The first 10 records on page number " + pageNumber + " are:");
 *     
 *     DalResponseRecordVisitor visitor = new DalResponseRecordVisitor() {
 *         int count = 0;
 *         public boolean visitResponseRecord(String tagName, DalResponseRecord record) {
 *             ++count;
 *
 *             System.out.println(tagName + "-" + count + ":");
 *             if (record.nestedData.size() &gt; 0) {
 *			       System.out.println(" there is some nested data");
 *			   }
 *
 *             for (String key : record.rowdata.keySet()) {
 *                 System.out.println(" " + key + "=" + record.rowdata.get(key));
 *             }
 *             return (count &lt;= 10); // Only visit the first 10 records
 *         }
 *     };
 *     page1.visitResults(visitor, "TrialUnit");
 *
 *
 *     // <b>Step 4:</b> Display the details for a specific ItemUnit using XML.
 *     //         This command is so simple we do not need to use <b>CommandBuilder</b>.
 *
 *     client.setResponseType(ResponseType.XML); // change to XML
 *     client.performQuery("get/itemunit/"+128).printOn(System.out);
 *     //
 *     // Result is something like:
 *     // &lt;?xml version="1.0" encoding="UTF-8" standalone="no"?&gt;
 *     // &lt;DATA&gt;
 *     // &lt;RecordMeta TagName="ItemUnit"/&gt;
 *     // &lt;ItemUnit GramsConversionMultiplier="1" ItemUnitId="128" ItemUnitName="U_8547569" ItemUnitNote="" update="update/itemunit/128"/&gt;
 *     // &lt;/DATA&gt;
 *
 * } catch (DalLoginException | DalResponseException | IOException | DalMissingParameterException e) {
 *     // We might have had a problem logging in (DalLoginException),
 *     // with the format of a DAL server response (DalResponseException),
 *     // with the actual server communication (IOException)
 *     // or mis-typed or left-out one of the parameters to <b>CommandBuilder</b> (DalMissingParameterException).
 *     e.printStackTrace();
 * } finally {
 *     // Make sure that we finish off the session.
 *     // If we didn't get logged in, this is a NO-OP.
 *     client.logout();   // Note that DefaultDALClient.finalize() also does this
 * }
 * </pre>
 */
package com.diversityarrays.dalclient;

