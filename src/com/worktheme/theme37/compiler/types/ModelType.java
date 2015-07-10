/*******************************************************************************
 * Copyright (c) 2012 Venkat Reddy
 *******************************************************************************/
package com.worktheme.theme37.compiler.types;

public enum ModelType {
	data, work, people, payment, session;

	public static ModelType getType(int i) {
		try {
			return values()[i];
		} catch (final ArrayIndexOutOfBoundsException e) {
			return data;
		}
	}

	public static ModelType getType(String typeId) {
		if (typeId == null)
			return data;
		if ("work".equalsIgnoreCase(typeId)) {
			return work;
		} else if ("people".equalsIgnoreCase(typeId)) {
			return people;
		} else if ("payment".equalsIgnoreCase(typeId)) {
			return payment;
		} else if ("session".equalsIgnoreCase(typeId)) {
			return session;
		} else if ("data".equalsIgnoreCase(typeId)) {
			return data;
		}
		return data;
	}
}
