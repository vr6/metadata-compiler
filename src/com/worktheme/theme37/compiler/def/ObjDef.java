/*******************************************************************************
 * Copyright (c) 2012 Venkat Reddy
 *******************************************************************************/
package com.worktheme.theme37.compiler.def;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jdom2.Element;

import com.worktheme.theme37.compiler.Database;

public class ObjDef {
	public List<Col> cols;
	public String tblName;
	public List<ObjDef> chs;
	public Database appDb;
	public String tagName;
	public List<ObjDef> parentObjs;

	public ObjDef(String tbl, String tag, Database db) {
		this.tblName = tbl;
		this.tagName = tag;
		this.appDb = db;
		this.chs = new ArrayList<ObjDef>();
		this.parentObjs = new ArrayList<ObjDef>();
		this.cols = new ArrayList<Col>();
	}

	public void load(Map<String, ObjDef> objdefs) {
		for (final Col col : this.cols) {
			final String pTag = col.getParentTag();
			if (pTag != null) {
				System.out.println("parent tag for:" + this.tagName + " is "
						+ pTag);
				final ObjDef objdef = objdefs.get(pTag);
				if (objdef == null)
					System.out.println("Parent element definition " + pTag
							+ " for " + this.tagName + " is not found.");
				else {
					System.out.println("Adding " + this.tagName
							+ " as childEl of " + objdef.tagName);
					objdef.addCh(this);
				}
			}
		}
	}

	public void addCol(Col col) {
		this.cols.add(col);
	}

	void addCh(ObjDef ch) {
		this.chs.add(ch);
	}

	@SuppressWarnings("unchecked")
	public int insert(Element el, int seqNum, int parentPk, String parentTag)
			throws Exception {
		System.out.println("adding row: " + el.getName() + ":" + seqNum);
		final StringBuffer buf = new StringBuffer();
		buf.append("insert into " + this.tblName + " (");
		for (final Col col : this.cols) {
			buf.append(col.getId() + ",");
		}
		buf.deleteCharAt(buf.length() - 1);
		buf.append(") values (");
		for (final Col col : this.cols) {
			String val = col.getVal(el, parentPk, parentTag);
			System.out.println(col.getId() + ":" + val);
			if (((val == null) || val.isEmpty()) && col.getId().equals("id")) {
				val = "z" + this.appDb.nextId();
			}
			buf.append(val + ",");
		}
		buf.deleteCharAt(buf.length() - 1);
		buf.append(");");
		System.out.println("objdef-insert:" + buf.toString());
		final int n = this.appDb.insert(this.tblName, buf.toString());
		System.out.println("adding children (" + this.chs.size() + ") for: "
				+ this.tagName);
		for (final ObjDef ch : this.chs) {
			final List<Element> chsEl = el.getChildren(ch.tagName);
			int seqN = 1;
			for (final Element cEl : chsEl) {
				System.out.println("ch:" + cEl.getName());
				ch.insert(cEl, seqN++, n, this.tagName);
			}
		}
		return n;
	}

	@Override
	public String toString() {
		return "ObjDef [cols=" + this.cols + ", tblName=" + this.tblName
				+ ", chs=" + this.chs + ", appDb=" + this.appDb + ", tagName="
				+ this.tagName + "]";
	}
}
