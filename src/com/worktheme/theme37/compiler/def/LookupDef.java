/*******************************************************************************
 * Copyright (c) 2012 Venkat Reddy
 *******************************************************************************/
package com.worktheme.theme37.compiler.def;

import java.util.HashMap;
import java.util.Map;

import org.jdom2.Element;

public class LookupDef {
	private final String id;
	private final Map<String, Integer> vals;

	public LookupDef(String id, String data) {
		this.id = id;
		this.vals = new HashMap<String, Integer>();
		final String[] t = data.split(",");
		int i = 0;
		for (final String val : t) {
			this.vals.put(val.toLowerCase(), i++);
		}
	}

	public LookupDef(Element el, String id) {
		this.id = id;
		this.vals = new HashMap<String, Integer>();
		final String data = el.getValue().trim();
		final String[] t = data.split(",");
		int i = 0;
		for (final String val : t) {
			this.vals.put(val, i++);
		}
	}

	public String getId() {
		return this.id;
	}

	public int getIndex(String key) {
		System.out.println(key + "=" + this.vals.get(key));
		return this.vals.get(key);
	}

	@Override
	public String toString() {
		return "LookupDef [id=" + this.id + ", vals=" + this.vals + "]";
	}
}
