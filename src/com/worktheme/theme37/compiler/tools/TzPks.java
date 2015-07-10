/*******************************************************************************
 * Copyright (c) 2012 Venkat Reddy
 *******************************************************************************/
package com.worktheme.theme37.compiler.tools;

import java.util.List;

import com.worktheme.theme37.compiler.Database;

public class TzPks {
	public static void main(String[] args) throws Exception {
		final Database db = new Database("appdata.sqlite");
		List<Object[]> qr = db.query("select rowid, id from tz_obj;", 2);
		for (final Object[] obj : qr) {
			System.out.print("\tpublic static final int PK_"
					+ obj[1].toString().toUpperCase() + " = " + obj[0] + ";\n");
		}
		System.out.println();
		qr = db.query("select rowid, id from tz_coltype;", 2);
		for (final Object[] obj : qr) {
			System.out.print("\tpublic static final int PK_COLTYPE_"
					+ obj[1].toString().toUpperCase() + " = " + obj[0] + ";\n");
		}
		System.out.println();
		qr = db.query("select rowid, id from tz_action;", 2);
		for (final Object[] obj : qr) {
			System.out.print("\tpublic static final int PK_MENU_ACTION_"
					+ obj[1].toString().toUpperCase() + " = " + obj[0] + ";\n");
		}
		System.out.println();
		qr = db.query(
				"select c.rowid, c.id from tz_col c, tz_obj t where c.obj = t.rowid and t.id = 'tz_col';",
				2);
		for (final Object[] obj : qr) {
			System.out.print("\tpublic static final int PK_TZ_COL_"
					+ obj[1].toString().toUpperCase() + " = " + obj[0] + ";\n");
		}

	}
}
