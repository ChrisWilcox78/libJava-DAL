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
package com.diversityarrays.daldb;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.diversityarrays.dalclient.CsvDalResponse;
import com.diversityarrays.dalclient.DALClient;
import com.diversityarrays.dalclient.DalMissingParameterException;
import com.diversityarrays.dalclient.DalResponse;
import com.diversityarrays.dalclient.DalResponseException;
import com.diversityarrays.dalclient.DalResponseRecord;
import com.diversityarrays.dalclient.DalResponseRecordVisitor;
import com.diversityarrays.dalclient.DalUtil;
import com.diversityarrays.dalclient.QueryBuilder;

public class SampleMeasurementHelper {

	static private final long DEFAULT_CSV_DOWNLOAD_DELAY_MILLIS = 1000;
	
	static public final long CSV_DOWNLOAD_DELAY_MILLIS;
	static {
		String pname = SampleMeasurementHelper.class.getName()+".CSV_DOWNLOAD_DELAY_MILLIS";
		String s = System.getProperty(pname);
		long millis = DEFAULT_CSV_DOWNLOAD_DELAY_MILLIS;
		try {
			if (s != null && ! s.isEmpty()) {
				millis = Long.parseLong(s, 10);
				millis = Math.max(0, millis);
			}
			else {
				Logger.getLogger(SampleMeasurementHelper.class.getName()).warning("Ignored empty value of "+pname);
			}
		}
		catch (NumberFormatException e) {
			Logger.getLogger(SampleMeasurementHelper.class.getName()).warning("Invalid value for "+pname+": '"+s+"'");
		}

		CSV_DOWNLOAD_DELAY_MILLIS = millis;

		Logger.getLogger(SampleMeasurementHelper.class.getName()).info(SampleMeasurementHelper.class.getName()+": Using CSV_DOWNLOAD_DELAY_MILLIS=" + CSV_DOWNLOAD_DELAY_MILLIS);
	}
	
	public static final String EXPORT_SAMPLEMEASUREMENT_CSV = "export/samplemeasurement/csv";

	public static final String IMPORT_SAMPLEMEASUREMENT_CSV = "import/samplemeasurement/csv";

	// trialunitid,sampletypeid,traitid,measurementdatetime,instancenumber,traitvalue
	
	static final List<String> ALL_HEADINGS;
	static {
		ALL_HEADINGS = Collections.unmodifiableList(Arrays.asList(
				"TrialUnitId",
				"SampleTypeId",
				"TraitId",
				"OperatorId",
				"MeasureDateTime",
				"InstanceNumber",
				// The above are all tagged @Id
				"TraitValue"
				));
	}
	
//	static private final String CRLF = "\r\n";
	
	static public String[] getAllHeadings() {
		return ALL_HEADINGS.toArray(new String[ALL_HEADINGS.size()]);
	}

// TODO re-instate all of these when I revert to using interface/impl for the DartEntity classes.	
//	static public void writeSampleMeasurements(Iterable<SampleMeasurement> iterable, OutputStream os)
//	throws IOException {
//		writeSampleMeasurements(iterable, new OutputStreamWriter(os), null, true);
//	}
//
//	static public void writeSampleMeasurements(Iterable<SampleMeasurement> iterable, OutputStream os, boolean autoClose)
//	throws IOException {
//		writeSampleMeasurements(iterable, new OutputStreamWriter(os), null, autoClose);
//	}
//
//	static public void writeSampleMeasurements(Iterable<SampleMeasurement> iterable, Writer w)
//	throws IOException {
//		writeSampleMeasurements(iterable, w, null, true);
//	}
//	
//	static public void writeSampleMeasurements(Iterable<SampleMeasurement> iterable, Writer w,
//			StringBuilder headingsLine, boolean autoClose) 
//	throws IOException {
//		
//		String firstLine = StringUtil.join(",", ALL_HEADINGS);
//		
//		if (headingsLine != null) {
//			headingsLine.append(firstLine);
//		}
//		
//		w.write(firstLine);
//		w.write(CRLF);
//		
//		DateFormat df = new SimpleDateFormat(DalUtil.DATE_FORMAT_STRING);
//		
//		for (SampleMeasurement sm : iterable) {
//			Date dateTime = sm.getMeasureDateTime();
//			StringBuilder sb = new StringBuilder();
//			sb.append(sm.getTrialUnitId())
//				.append(',').append(sm.getSampleTypeId())
//				.append(',').append(sm.getTraitId())
//				.append(',').append(sm.getOperatorId())
//				.append(',').append(dateTime==null ? "" : df.format(dateTime))
//				.append(',').append(sm.getInstanceNumber())
//				;
//
//			String traitValue = sm.getTraitValue();
//			if (traitValue.indexOf(',')>=0 || traitValue.indexOf('"')>=0) {
//				sb.append(",\"").append(StringUtil.doubleUpDoubleQuote(traitValue)).append('"');
//			}
//			else {
//				sb.append(traitValue);
//			}
//			sb.append(CRLF);
//			
//			w.write(sb.toString());
//		}
//		
//		if (autoClose) {
//			w.close();
//		}
//		else {
//			w.flush();
//		}
//	}
	
//	static public DalResponse uploadSampleMeasurements(Iterable<SampleMeasurement> sampleMeasurements, DALClient client)
//	throws IOException, DalResponseException {
//		final StringWriter sw = new StringWriter();
//		PrintWriter pw = new PrintWriter(sw);
//		
//		StringBuilder headingsLine = new StringBuilder();
//		writeSampleMeasurements(sampleMeasurements, pw, headingsLine, true);
//		
//		CsvHeadingMapper csvHeadingMapper = new CsvHeadingMapper(getAllHeadings());
//
//		Factory<InputStream> streamFactory = new Factory<InputStream>() {
//			@Override
//			public InputStream create() {
//				return new StringInputStream(sw.toString());
//			}
//		};
//
//		try {
//			Map<String, String> postParams = 
//					csvHeadingMapper.createHeadingToColumnIndex("sampleMeasurements", headingsLine.toString());
//	
//			return client.prepareUpload(IMPORT_SAMPLEMEASUREMENT_CSV, streamFactory)
//				.addPostParameters(postParams)
//				.execute();
//		} catch (MissingFieldException e) {
//			throw new RuntimeException(e);
//		}
//	}
	

	static public DalResponse performDownloadSampleMeasurement(DALClient client,
			Collection<Integer> trialUnitIds,
			Collection<Integer> traitIds,
			final SampleMeasurementConsumer sampleMeasurementConsumer /*,
			final Transformer<List<ValueConversionProblem>,Boolean> problemConsumer*/) 
	throws DalResponseException, DalMissingParameterException, IOException
	{
		return performDownloadSampleMeasurement(client,
				trialUnitIds, traitIds, sampleMeasurementConsumer, // problemConsumer,
				CSV_DOWNLOAD_DELAY_MILLIS);
	}
	
	static public DalResponse performDownloadSampleMeasurement(DALClient client,
			Collection<Integer> trialUnitIds,
			Collection<Integer> traitIds,
			final SampleMeasurementConsumer sampleMeasurementConsumer,
			/* final Transformer<List<ValueConversionProblem>,Boolean> problemConsumer, */
			long csvDownloadDelay)
	throws DalResponseException, IOException, DalMissingParameterException {
		
		if (sampleMeasurementConsumer==null) {
			throw new IllegalArgumentException("sampleMeasurementConsumer must not be null");
		}
		
		if (trialUnitIds==null || trialUnitIds.isEmpty()) {
			throw new DalMissingParameterException("No TrialUnitIds supplied");
		}
		
		QueryBuilder builder = client.prepareQuery(EXPORT_SAMPLEMEASUREMENT_CSV)
				.setParameter("TrialUnitIdCSV", DalUtil.join(",", trialUnitIds));
		if (traitIds!=null && ! traitIds.isEmpty()) {
			builder.setParameter("TraitIdCSV", DalUtil.join(",", traitIds));
		}
		
		DalResponse response = builder.execute();
		
		String url = response.getRecordFieldValue(DALClient.TAG_RETURN_ID_FILE, DALClient.ATTR_XML);
		
		try {
			Thread.sleep(CSV_DOWNLOAD_DELAY_MILLIS);
		}
		catch (InterruptedException ignore) {}
		
		DalResponse urlResponse = client.performQuery(url);
		if (urlResponse instanceof CsvDalResponse) {
			CsvDalResponse csvResponse = (CsvDalResponse) urlResponse;
			csvResponse.setUseHeadings(true);
			
//			final DartEntityBuilder<SampleMeasurement> deb = new DartEntityBuilder<SampleMeasurement>(SampleMeasurement.class, null);
			
			csvResponse.visitResults(new DalResponseRecordVisitor() {
				@Override
				public boolean visitResponseRecord(String resultTagName, DalResponseRecord record) {
//					List<ValueConversionProblem> problems = new ArrayList<ValueConversionProblem>();
//					SampleMeasurement sm = deb.build(record.rowdata, null, problems);
					
					return sampleMeasurementConsumer.consume(record.rowdata, null /*, problems*/);
					
//					if (! problems.isEmpty()) {
//					return problemConsumer==null || problemConsumer.transform(problems);
//					}

				}
			});
			return null;
		}
		
		return urlResponse;
	}
}
