/*******************************************************************************
 * Copyright (c) 2012 Venkat Reddy
 *******************************************************************************/
package com.worktheme.theme37.compiler.types;

public enum ExpType {
	exp, total, count, max, min, average, sql, clz, rel;
	public static ExpType getType(int i) {
		try {
			return values()[i];
		} catch (final ArrayIndexOutOfBoundsException e) {
			return exp;
		}
	}

	public static ExpType getType(String typeId) {
		if (typeId == null)
			return null;
		if ("sum".equalsIgnoreCase(typeId)) {
			return total;
		} else if ("count".equalsIgnoreCase(typeId)) {
			return count;
		} else if ("max".equalsIgnoreCase(typeId)) {
			return max;
		} else if ("min".equalsIgnoreCase(typeId)) {
			return min;
		} else if ("average".equalsIgnoreCase(typeId)) {
			return average;
		} else if ("sql".equalsIgnoreCase(typeId)) {
			return sql;
		} else if ("clz".equalsIgnoreCase(typeId)) {
			return clz;
		} else if ("rel".equalsIgnoreCase(typeId)) {
			return rel;
		}
		return exp;
	}
}
