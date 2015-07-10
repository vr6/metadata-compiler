/*******************************************************************************
 * Copyright (c) 2012 Venkat Reddy
 *******************************************************************************/
package com.worktheme.theme37.compiler.types;

public enum TblType {
	systbl, apptbl, apptblext, datatbl;

	public static TblType getType(int i) {
		try {
			return values()[i];
		} catch (final ArrayIndexOutOfBoundsException e) {
			return datatbl;
		}
	}

	public static TblType getType(String typeId) {
		if (typeId == null)
			return datatbl;
		if ("systbl".equalsIgnoreCase(typeId)) {
			return systbl;
		} else if ("apptbl".equalsIgnoreCase(typeId)) {
			return apptbl;
		} else if ("apptblext".equalsIgnoreCase(typeId)) {
			return apptblext;
		}
		return datatbl;
	}
}
