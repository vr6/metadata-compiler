/*******************************************************************************
 * Copyright (c) 2012 Venkat Reddy
 *******************************************************************************/
package com.worktheme.theme37.compiler.types;

public enum RelationType {
	MANY_TO_ONE, ONE_TO_MANY, MANY_TO_MANY, CONNECTION, ONE_TO_ONE, UNDEFINED;
	public static RelationType getType(int i) {
		try {
			return values()[i];
		} catch (final ArrayIndexOutOfBoundsException e) {
			return UNDEFINED;
		}
	}

	public static RelationType getType(String typeId) {
		if (typeId == null)
			return UNDEFINED;
		if ("many-to-one".equalsIgnoreCase(typeId)) {
			return MANY_TO_ONE;
		} else if ("one-to-many".equalsIgnoreCase(typeId)) {
			return ONE_TO_MANY;
		} else if ("many-to-many".equalsIgnoreCase(typeId)) {
			return MANY_TO_MANY;
		}
		return UNDEFINED;
	}
}
