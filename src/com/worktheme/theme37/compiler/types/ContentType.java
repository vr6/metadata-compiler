/*******************************************************************************
 * Copyright (c) 2012 Venkat Reddy
 *******************************************************************************/
package com.worktheme.theme37.compiler.types;

public enum ContentType {
	contact, image, video, music, weblink, file, undefined;
	public static ContentType getType(int i) {
		try {
			return values()[i];
		} catch (final ArrayIndexOutOfBoundsException e) {
			return undefined;
		}
	}

	public static ContentType getType(String typeId) {
		if (typeId == null)
			return null;
		if ("contact".equalsIgnoreCase(typeId)) {
			return contact;
		} else if ("image".equalsIgnoreCase(typeId)) {
			return image;
		} else if ("video".equalsIgnoreCase(typeId)) {
			return video;
		} else if ("music".equalsIgnoreCase(typeId)) {
			return music;
		} else if ("weblink".equalsIgnoreCase(typeId)) {
			return weblink;
		} else if ("file".equalsIgnoreCase(typeId)) {
			return file;
		}
		return undefined;
	}
}
