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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.diversityarrays.dalclient.DalResponse;
import com.diversityarrays.dalclient.DalResponseException;
import com.diversityarrays.dalclient.DalResponseRecord;
import com.diversityarrays.dalclient.DalResponseRecordVisitor;

/**
 * Retrieved information from the DAL command "trial/_trialid/export/datakapturetemplate".
 * @author brian
 *
 */
// TODO check if we need this XML bit
// TODO move to TrialUtils
@XmlAccessorType(XmlAccessType.FIELD)
public class DataCaptureTemplateInfo {
	
	private int traitColumnCount;
	private int trialUnitCount;
	
	private String templateFileUrl;
	private String traitFileUrl;
	
	// For JAXB
	public DataCaptureTemplateInfo() {
	}
	
	public DataCaptureTemplateInfo(final DalResponse exportCommandResponse) throws DalResponseException {
		traitColumnCount = Integer.parseInt(exportCommandResponse.getRecordFieldValue("Info", "NumOfTraitColumn"), 10);
		trialUnitCount = Integer.parseInt(exportCommandResponse.getRecordFieldValue("Info", "NumOfTrialUnit"), 10);
		DalResponseRecordVisitor visitor = new DalResponseRecordVisitor() {
			@Override
			public boolean visitResponseRecord(String resultTagName, DalResponseRecord record) {
				String fileId = record.rowdata.get("FileIdentifier");
				if ("TemplateFile".equals(fileId)) {
					templateFileUrl = record.rowdata.get("csv");
				}
				else if ("TraitFile".equals(fileId)) {
					traitFileUrl = record.rowdata.get("csv");
				}
				else {
					System.err.println("Unsupported FileIdentifier '" + fileId + "' for " + exportCommandResponse.getUrl());
				}
				return true;
			}
		};
		exportCommandResponse.visitResults(visitor, "OutputFile");
	}
	
	@Override
	public String toString() {
		return traitColumnCount + " traits in " + trialUnitCount + " trial units";
	}

	public int getTraitColumnCount() {
		return traitColumnCount;
	}

	public int getTrialUnitCount() {
		return trialUnitCount;
	}

	public String getTemplateFileUrl() {
		return templateFileUrl;
	}

	public String getTraitFileUrl() {
		return traitFileUrl;
	}
	
	
}