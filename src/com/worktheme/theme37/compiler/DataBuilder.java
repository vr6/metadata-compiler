/*******************************************************************************
 * Copyright (c) 2012 Venkat Reddy
 *******************************************************************************/
package com.worktheme.theme37.compiler;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import com.worktheme.theme37.compiler.def.ColDef;
import com.worktheme.theme37.compiler.def.EnumDef;
import com.worktheme.theme37.compiler.def.LookupDef;
import com.worktheme.theme37.compiler.def.ObjDef;
import com.worktheme.theme37.compiler.tools.Util;

public class DataBuilder {
	Database appDb;
	Map<String, LookupDef> lookups;
	Map<String, ObjDef> objdefs;
	String[] bpmTbls = { "tz_lookup", "tz_coltype", "tz_obj", "tz_folder",
			"tz_fvtab", "tz_rel", "tz_col", "tz_action", "tz_shortcut",
			"tz_config" };

	public DataBuilder(Database tgt) throws Exception {
		this.appDb = tgt;
		this.lookups = new HashMap<String, LookupDef>();
		this.objdefs = new HashMap<String, ObjDef>();

		List<Object[]> qr = this.appDb.query("select id, data from tz_lookup;",
				2);
		for (final Object[] obj : qr) {
			final LookupDef lkpdef = new LookupDef((String) obj[0],
					(String) obj[1]);
			this.lookups.put((String) obj[0], lkpdef);
		}

		final StringBuffer buf = new StringBuffer();
		for (final String tbl : this.bpmTbls) {
			buf.append("'" + tbl + "',");
		}
		buf.deleteCharAt(buf.length() - 1);
		qr = this.appDb.query("select id, tagName from tz_obj where id in ("
				+ buf.toString() + ");", 2);
		for (final Object[] obj : qr) {
			final ObjDef objdef = new ObjDef((String) obj[0], (String) obj[1],
					this.appDb);
			this.objdefs.put((String) obj[1], objdef);

			final String s = "select t.id, c.id, c.defValue, e.id, ct.id, c.required, c.fkeyRel, ct.sqltype, ct.rowid "
					+ "from tz_col c left join tz_obj t on t.rowid  = c.obj left join tz_lookup e on e.rowid = c.fkeyRel left join tz_coltype ct on ct.rowid = c.dataType "
					+ "where t.id = '" + (String) obj[0] + "'";
			final List<Object[]> qr2 = this.appDb.query(s, 9);
			for (final Object[] obj2 : qr2) {
				if (Tz.PK_COLTYPE_REF == (Integer) obj2[8]) {
				} else if (Tz.PK_COLTYPE_DROPDOWN == (Integer) obj2[8]) {
					final LookupDef lkp = this.lookups.get(obj2[3]);
					objdef.addCol(new EnumDef((String) obj2[1], lkp,
							(String) obj2[2], (String) obj2[4]));
				} else {
					final ColDef col = new ColDef((String) obj2[1],
							(Integer) obj2[7] == 0, (String) obj2[2],
							(String) obj2[4], (Integer) obj2[5] == 1);
					objdef.addCol(col);
				}
			}

		}
		for (final ObjDef obj : this.objdefs.values()) {
			obj.load(this.objdefs);
		}
	}

	public static void main(String args[]) throws Exception {
		if (args.length < 3) {
			usage();
		}
		final File f = new File(args[2]);
		if (f.exists()) {
			if (!f.delete()) {
				System.out.println("ERROR: File could not be deleted: "
						+ f.getName());
				System.exit(1);
			}
			if (!f.createNewFile()) {
				System.out.println("ERROR: File could not be created: "
						+ f.getName());
				System.exit(1);
			}
		}
		final File m = new File(args[0]);
		Util.copyFile(m, f);
		final Database tgt = new Database(args[2]);
		new DataBuilder(tgt).loadFromXML(args[1]);
		tgt.close();

	}

	@SuppressWarnings("unchecked")
	public void loadFromXML(String xmlName) throws Exception {
		System.out.println("Building app from XML : " + xmlName);
		final SAXBuilder builder = new SAXBuilder();
		final File objFile = new File(xmlName);
		if (!objFile.exists()) {
			System.out.println("Error reading the XML file.");
			usage();
		}
		final Document docObj = builder.build(objFile);
		final Element om = docObj.getRootElement();
		final Element dataEl = om.getChild("data");
		final List<Element> dataList = dataEl.getChildren();
		for (final Element el : dataList) {
			final ObjDef obj = this.objdefs.get(el.getName());
			if (obj == null) {
				System.out.println("ERROR: Object definition not found for :"
						+ el.getName());
			} else {
				obj.insert(el, 1, 0, null);
			}
		}

		System.out.println("pending updates: "
				+ PendingUpdate.getUpdates().size());
		int res = 0;
		for (final PendingUpdate u : PendingUpdate.getUpdates()) {
			System.out.println("getpk=" + "select rowid from " + u.tgtTbl
					+ " where id='" + u.idVal + "'");
			final int ret = this.appDb.getPk("select rowid from " + u.tgtTbl
					+ " where id='" + u.idVal + "'");
			if (ret > 0) {
				final String s = "update " + u.srcTbl + " set " + u.srcCol
						+ " = " + ret + " where " + u.srcCol + " = " + u.pid;
				System.out.println("updatepk=" + s);
				this.appDb.execute(s);
				u.resolved = true;
				res++;
			} else {
				System.out.println("Could not resolve: " + u.toString());
			}
		}
		System.out.println("resolved: " + res);
		System.out.println("Finished building app from XML : " + xmlName);
	}

	private static void usage() {
		System.out
				.println("Usage : java XMLLoader <app db file> <user db file> <xml file having app schema>");
		System.exit(0);
	}
}
