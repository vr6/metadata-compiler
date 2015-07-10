/*******************************************************************************
 * Copyright (c) 2012 Venkat Reddy
 *******************************************************************************/
package com.worktheme.theme37.compiler.def;

import org.jdom2.Element;

public class EnumDef implements Col {
	public String id;
	public LookupDef lkp;
	public String defVal;
	public String clzName;
	public boolean bitflags;

	public EnumDef(String id, LookupDef lkp, String def, String wtype)
			throws Exception {
		this.id = id;
		this.defVal = def;
		this.lkp = lkp;
		this.bitflags = wtype.equals("multicheck");
	}

	@Override
	public String getVal(Element el, int parent, String parentTag)
			throws Exception {
		String val = el.getAttributeValue(this.id);
		if (val == null)
			val = this.defVal;

		if ("any".equals(val))
			return "" + Integer.MAX_VALUE;

		int res = 0;
		if (!this.bitflags) {
			return val == null ? "null" : ""
					+ this.lkp.getIndex(val.toLowerCase());
		} else {
			if (val != null) {
				if (!val.startsWith("!(")) {
					final String[] t = val.split(",");
					for (final String s : t) {
						res |= (1 << this.lkp.getIndex(s.toLowerCase()));
					}
				} else {
					final String val2 = val.substring(2, val.length() - 1);
					final String[] t = val2.split(",");
					res = Integer.MAX_VALUE;
					for (final String s : t) {
						res ^= (1 << this.lkp.getIndex(s.toLowerCase()));
					}
				}
			}
		}
		return "" + res;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public String toString() {
		return "EnumDef [id=" + this.id + ", lkp=" + this.lkp + ", defVal="
				+ this.defVal + ", clzName=" + this.clzName + "]";
	}

	@Override
	public String getParentTag() {
		return null;
	}
}
