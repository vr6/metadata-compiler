/*******************************************************************************
 * Copyright (c) 2012 Venkat Reddy
 *******************************************************************************/
package com.worktheme.theme37.compiler.def;

import org.jdom.Element;

public class ColDef implements Col {
	public String id;
	public boolean squote;
	public boolean boolVal;
	public boolean required;
	public String defVal;

	public ColDef(String id, boolean sq, String def, String bVal,
			boolean required) {
		this.id = id;
		this.squote = sq;
		this.defVal = def;
		this.boolVal = bVal.equals("checkbox");
		this.required = required;
	}

	@Override
	public String getVal(Element el, int parent, String parentTag) {
		final String val = el.getAttributeValue(this.id);
		if ((val == null) && this.required) {
			System.out.println("The element " + el.getName()
					+ " is missing a required attribute: " + this.id);
		}
		System.out.println("getVal:val=" + val + ":defVal=" + this.defVal);
		if (this.boolVal) {
			boolean res;
			if (val == null)
				res = this.defVal == null ? false : Boolean
						.parseBoolean(this.defVal);
			else
				res = Boolean.parseBoolean(val);
			return res ? "1" : "0";
		} else {
			if (val == null)
				return this.defVal == null ? "null" : this.squote ? "'"
						+ this.defVal + "'" : this.defVal;
			else
				return this.squote ? "'" + val + "'" : val;
		}
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public String toString() {
		return "ColDef [id=" + this.id + ", squote=" + this.squote
				+ ", boolVal=" + this.boolVal + ", required=" + this.required
				+ ", defVal=" + this.defVal + "]";
	}

	@Override
	public String getParentTag() {
		return null;
	}
}
