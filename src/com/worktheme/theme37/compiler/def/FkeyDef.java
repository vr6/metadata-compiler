/*******************************************************************************
 * Copyright (c) 2012 Venkat Reddy
 *******************************************************************************/
package com.worktheme.theme37.compiler.def;

import org.jdom.Element;

import com.worktheme.theme37.compiler.Database;
import com.worktheme.theme37.compiler.PendingUpdate;

public class FkeyDef implements Col {
	private final String id;
	public Database appDb;
	private final String robj;
	public String parentTag;
	String tbl;

	public FkeyDef(Database db, String srcObj, String id, String robj,
			String pTag) {
		this.id = id;
		this.appDb = db;
		this.parentTag = pTag;
		this.robj = robj;
		this.tbl = srcObj;
	}

	@Override
	public String getVal(Element el, int parentPk, String pTag)
			throws Exception {
		if ((parentPk > 0) && (pTag != null) && pTag.equals(this.parentTag))
			return "" + parentPk;
		final String idVal = el.getAttributeValue(this.id);
		if (idVal == null)
			return "null";
		String sql = null;
		if (this.robj.equals("tz_col")) {
			final String obj = el.getAttributeValue("obj");
			sql = "select c.rowid from tz_col c, tz_obj t where c.obj = t.rowid and t.id='"
					+ obj + "' and c.id='" + idVal + "'";
		} else if (this.robj.equals("tz_rel")) {
			final String obj = el.getAttributeValue("obj");
			sql = "select r.rowid from tz_rel r, tz_obj t where r.obj = t.rowid and t.id='"
					+ obj + "' and r.id='" + idVal + "'";
		}
		return "" + getPkById(this.tbl, this.id, this.robj, idVal, sql);
	}

	@Override
	public String getId() {
		return this.id;
	}

	public int getPkById(String srcTbl, String srcCol, String tgtTbl,
			String idVal, String sql) throws Exception {
		if (idVal != null) {
			String s = "select rowid from " + tgtTbl + " where id='" + idVal
					+ "'";
			if (sql != null) {
				s = sql;
			}
			final int ret = this.appDb.getPk(s);
			if (ret < 0) {
				System.out.println("adding pu");
				final PendingUpdate pu = new PendingUpdate(srcTbl, srcCol,
						tgtTbl, idVal);
				PendingUpdate.add(pu);
				return pu.pid;
			} else {
				return ret;
			}
		}
		return 0;
	}

	@Override
	public String toString() {
		return "FkeyDef [id=" + this.id + ", appDb=" + this.appDb + ", robj="
				+ this.robj + "]";
	}

	@Override
	public String getParentTag() {
		return this.parentTag;
	}
}
