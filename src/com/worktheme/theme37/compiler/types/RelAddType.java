/*******************************************************************************
 * Copyright (c) 2012 Venkat Reddy
 *******************************************************************************/
package com.worktheme.theme37.compiler.types;

public enum RelAddType {
	any, addnew, addfree;

	public static RelAddType getType(String typeId) {
		if (typeId == null)
			return any;
		if ("Any".equalsIgnoreCase(typeId)) {
			return any;
		} else if ("New Only".equalsIgnoreCase(typeId)) {
			return addnew;
		} else if ("Free Only".equalsIgnoreCase(typeId)) {
			return addfree;
		}
		return any;
	}
}
