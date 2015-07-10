/*******************************************************************************
 * Copyright (c) 2012 Venkat Reddy
 *******************************************************************************/
package com.worktheme.theme37.compiler.types;

public enum ModelColType {
	col, fkey, mkey, pointer, rel, mrel, undefined;

	public static ModelColType getType(int i) {
		try {
			return values()[i];
		} catch (final ArrayIndexOutOfBoundsException e) {
			return undefined;
		}
	}

	public static ModelColType getType(String typeId) {
		if (typeId == null)
			return null;
		if ("col".equalsIgnoreCase(typeId)) {
			return col;
		} else if ("fkey".equalsIgnoreCase(typeId)) {
			return fkey;
		} else if ("mkey".equalsIgnoreCase(typeId)) {
			return mkey;
		} else if ("pointer".equalsIgnoreCase(typeId)) {
			return pointer;
		} else if ("rel".equalsIgnoreCase(typeId)) {
			return rel;
		} else if ("mrel".equalsIgnoreCase(typeId)) {
			return mrel;
		}
		return col;
	}
}
