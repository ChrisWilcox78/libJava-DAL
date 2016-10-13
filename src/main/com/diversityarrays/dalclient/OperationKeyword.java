package com.diversityarrays.dalclient;

public enum OperationKeyword {
	FILTERING("Filtering"), //$NON-NLS-1$
	SORTING("Sorting"), //$NON-NLS-1$
	GROUP_BY_FIELD("GroupByField"), //$NON-NLS-1$
	;
	
	public final String value;
	OperationKeyword(String s) {
		this.value = s;
	}
	
	@Override
	public String toString() {
		return value;
	}
}
