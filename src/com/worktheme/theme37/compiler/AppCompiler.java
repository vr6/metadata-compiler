/*******************************************************************************
 * Copyright (c) 2012 Venkat Reddy
 *******************************************************************************/
package com.worktheme.theme37.compiler;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import com.worktheme.theme37.compiler.def.LookupDef;
import com.worktheme.theme37.compiler.tools.Util;
import com.worktheme.theme37.compiler.types.ContentType;
import com.worktheme.theme37.compiler.types.ModelType;
import com.worktheme.theme37.compiler.types.RelAddType;
import com.worktheme.theme37.compiler.types.RelationType;
import com.worktheme.theme37.compiler.types.SqlType;
import com.worktheme.theme37.compiler.types.TblType;

public class AppCompiler {
	Database appDb;
	List<PendingUpdate> updates;
	private static int pidx = -1;
	private static int lastPk = 0;
	Map<String, LookupDef> lookups;

	public AppCompiler(Database tgt) throws Exception {
		this.appDb = tgt;
		this.updates = new ArrayList<PendingUpdate>();
		this.lookups = new HashMap<String, LookupDef>();
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
		new AppCompiler(tgt).loadFromXML(args[1]);
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
		final StringBuilder sqlBuf = new StringBuilder();

		final Map<Element, Ret> pks = new HashMap<Element, Ret>();
		final List<Element> objs = om.getChildren("obj");
		for (final Element objEl : objs) {
			pks.put(objEl, addTable(objEl));
		}
		for (final Element objEl : objs) {
			final Ret r = pks.get(objEl);
			final int objPk = r.pk;

			sqlBuf.append("create table if not exists '" + r.id + "' (");
			final List<Object[]> qr = this.appDb.query(
					"select a.id, ct.sqltype from tz_col a, tz_obj b, tz_coltype ct "
							+ "where a.dataType <> " + Tz.PK_COLTYPE_COMPUTED
							+ " and a.obj = b.rowid and b.rowid = " + objPk
							+ " and a.dataType = ct.rowid", 2);
			for (final Object[] obj : qr) {
				sqlBuf.append("'" + obj[0] + "' "
						+ SqlType.getType((Integer) obj[1]) + ",");
			}
			sqlBuf.append("'name' text);\n");
		}

		System.out.println("\nbuf=" + sqlBuf);
		this.appDb.execute(sqlBuf.toString());
		System.out.println();

		System.out.println("pending updates: " + this.updates.size());
		int res = 0;
		for (final PendingUpdate u : this.updates) {

			String s = null;
			if (u.obj > 0) {
				s = "select c.rowid from " + u.tgtTbl
						+ " c, tz_obj t where c.id='" + u.idVal
						+ "' and t.rowid = c.obj and t.rowid = " + u.obj;
			} else {
				s = "select rowid from " + u.tgtTbl + " where id='" + u.idVal
						+ "'";
			}
			System.out.println("resolving: s = " + s);
			final int ret = this.appDb.getPk(s);
			if (ret > 0) {
				s = "update " + u.srcTbl + " set " + u.srcCol + " = " + ret
						+ " where " + u.srcCol + " = " + u.pid;
				System.out.println("updating: s = " + s);
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

	private void addModelAction(int objPk, Element actionEl) throws Exception {
		System.out.print("ma");
		final String name = actionEl.getAttributeValue("name");
		final String id = actionEl.getAttributeValue("id");
		final String idVal = actionEl.getAttributeValue("usertype");
		int utype = 0;
		if (idVal != null) {
			utype = Integer.valueOf(idVal);
		}
		final String src = actionEl.getAttributeValue("src");
		final String dest = actionEl.getAttributeValue("dest");

		final String s = "insert into tz_action (obj, id, name, src, dest, usertype) values ("
				+ objPk
				+ ","
				+ Util.squote(id)
				+ ","
				+ Util.squote(name)
				+ ","
				+ Util.squote(src)
				+ ","
				+ Util.squote(dest)
				+ ","
				+ utype
				+ ");";
		this.appDb.execute(s);
	}

	private void addRel(int objPk, Element relEl, int rtype) throws Exception {
		System.out.print("r");
		String id = relEl.getAttributeValue("id");
		if ((id == null) || id.isEmpty()) {
			id = "r" + this.appDb.nextId();
		}
		String name = relEl.getAttributeValue("name");
		if (name == null)
			name = relEl.getAttributeValue("name1");

		int obj1 = objPk;
		if (obj1 < 1) {
			System.out.println("obj1 < 1: ?");
			final String idVal = relEl.getAttributeValue("obj");
			obj1 = getPkById("tz_rel", "obj", "tz_obj", idVal, null, 0);
		}
		int obj2 = 0;
		String idVal = relEl.getAttributeValue("obj2");
		if (idVal != null)
			obj2 = getPkById("tz_rel", "obj2", "tz_obj", idVal, null, 0);

		String vtbl = null;
		if (rtype > 1) {
			vtbl = "v" + this.appDb.nextId();
			this.appDb.execute("create table if not exists '" + vtbl
					+ "' ('obj' integer, 'obj2' integer);\n");
		}
		final boolean readonly = getBoolean(relEl, "readonly", false);
		final String obj2Filter = relEl.getAttributeValue("obj2Filter");
		final boolean hidden = getBoolean(relEl, "hidden", false);
		final boolean listCol = getBoolean(relEl, "listCol", false);
		final int addtype = RelAddType.getType(
				relEl.getAttributeValue("addtype")).ordinal();

		String s = "insert into tz_rel (id, obj, obj2, type, obj2Filter, name, vtbl, pair, listCol, "
				+ "readonly, hidden, meta, addtype) values ("
				+ Util.squote(id)
				+ ","
				+ obj1
				+ ","
				+ obj2
				+ ","
				+ rtype
				+ ","
				+ Util.squote(obj2Filter)
				+ ","
				+ Util.squote(name)
				+ ","
				+ Util.squote(vtbl)
				+ ","
				+ (lastPk + 2)
				+ ","
				+ (listCol ? 1 : 0)
				+ ","
				+ (readonly ? 1 : 0)
				+ ","
				+ (hidden ? 1 : 0) + "," + 1 + "," + addtype + ");";
		lastPk = this.appDb.insert("tz_rel", s);

		final int rtype2 = rtype > 1 ? rtype : 1 - rtype;

		obj1 = obj2;
		if (obj1 < 1) {
			idVal = relEl.getAttributeValue("obj2");
			obj1 = getPkById("tz_rel", "obj", "tz_obj", idVal, null, 0);
		}
		obj2 = objPk;
		final String name2 = relEl.getAttributeValue("name2");

		final boolean readonly2 = getBoolean(relEl, "readonly2", false);
		final boolean listCol2 = getBoolean(relEl, "listCol2", false);
		final String obj2Filter2 = relEl.getAttributeValue("obj2Filter2");
		final boolean hidden2 = getBoolean(relEl, "hidden2", false);
		final int addtype2 = RelAddType.getType(
				relEl.getAttributeValue("addtype2")).ordinal();

		String id2 = relEl.getAttributeValue("id2");
		if ((id2 == null) || id2.isEmpty()) {
			id2 = "r" + this.appDb.nextId();
			;
		}

		s = "insert into tz_rel (id, obj, obj2, type, obj2Filter, name, vtbl, pair, listCol, "
				+ "meta, readonly, hidden, addtype) values ("
				+ Util.squote(id2)
				+ ","
				+ obj1
				+ ","
				+ obj2
				+ ","
				+ rtype2
				+ ","
				+ Util.squote(obj2Filter2)
				+ ","
				+ Util.squote(name2)
				+ ","
				+ Util.squote(vtbl)
				+ ","
				+ lastPk
				+ ","
				+ (listCol2 ? 1 : 0)
				+ ","
				+ 1
				+ ","
				+ (readonly2 ? 1 : 0)
				+ "," + (hidden2 ? 1 : 0) + "," + addtype2 + ");";
		lastPk = this.appDb.insert("tz_rel", s);
	}

	@SuppressWarnings("unchecked")
	private Ret addTable(Element objEl) throws Exception {
		String id = objEl.getAttributeValue("id");
		if ((id == null) || id.isEmpty()) {
			id = "t" + this.appDb.nextId();
			;
		}
		final String name = objEl.getAttributeValue("name");
		System.out.println();
		System.out.print("Loading object: " + name + " ");

		final String tagName = objEl.getAttributeValue("tagName");
		final TblType objtype = TblType
				.getType(objEl.getAttributeValue("type"));
		final int model = ModelType.getType(objEl.getAttributeValue("model"))
				.ordinal();

		final String s = "insert into tz_obj (id, name, tagname, "
				+ "tbltype, model, meta) values (" + Util.squote(id) + ","
				+ Util.squote(name) + "," + Util.squote(tagName) + ","
				+ objtype.ordinal() + "," + model + "," + 1 + ");";
		final int n = this.appDb.insert("tz_obj", s);

		final List<Element> chs = objEl.getChildren();
		for (final Element ch : chs) {
			if (ch.getName().equals("col")) {
				addColumn(n, ch, 0);
			} else if (ch.getName().equals("fkey")) {
				addRel(n, ch, RelationType.MANY_TO_ONE.ordinal());
				addColumn(n, ch, lastPk - 1);
			} else if (ch.getName().equals("rel")) {
				addRel(n, ch, RelationType.MANY_TO_MANY.ordinal());
			} else if (ch.getName().equals("action")) {
				addModelAction(n, ch);
			}
		}
		return new Ret(n, id, true);
	}

	private boolean getBoolean(Element el, String id, boolean def) {
		final String val = el.getAttributeValue(id);
		return val == null ? def : Boolean.parseBoolean(val);

	}

	private void addColumn(int tblPk, Element colEl, int fkeyRel)
			throws Exception {
		System.out.print("c");

		String idVal = colEl.getAttributeValue("dataType");
		final int ctype = getPkById("tz_col", "dataType", "tz_coltype", idVal,
				null, 0);

		if (fkeyRel == 0) {
			switch (ctype) {
			case Tz.PK_COLTYPE_DROPDOWN:
			case Tz.PK_COLTYPE_MULTICHECK:
				idVal = colEl.getAttributeValue("lookupData");
				fkeyRel = getPkById("tz_col", "fkeyRel", "tz_lookup", idVal,
						null, 0);
				break;
			case Tz.PK_COLTYPE_CONTENT:
				idVal = colEl.getAttributeValue("contentType");
				fkeyRel = ContentType.getType(idVal).ordinal();
				break;
			}
		}

		String id = colEl.getAttributeValue("id");
		if ((id == null) || id.isEmpty()) {
			id = "c" + this.appDb.nextId();
			;
		}

		final String name = colEl.getAttributeValue("name");
		final String desc = colEl.getAttributeValue("desc");
		final String validation = colEl.getAttributeValue("validation");
		final String defValue = colEl.getAttributeValue("defValue");
		final String showIf = colEl.getAttributeValue("showIf");
		final String hideIf = colEl.getAttributeValue("hideIf");

		final boolean required = getBoolean(colEl, "required", false);
		final boolean readonly = getBoolean(colEl, "readonly", false);
		final boolean hidden = getBoolean(colEl, "hidden", false);
		final boolean listCol = getBoolean(colEl, "listCol", false);
		final boolean addCol = getBoolean(colEl, "addCol", true);

		final String s = "insert into tz_col (id, obj, dataType, name, validation, showIf, hideIf, "
				+ "defValue, fkeyRel, desc, readonly, required, "
				+ "listCol, addCol, hidden, meta) values ("
				+ Util.squote(id)
				+ ","
				+ tblPk
				+ ","
				+ ctype
				+ ","
				+ Util.squote(name)
				+ ","
				+ Util.squote(validation)
				+ ","
				+ Util.squote(showIf)
				+ ","
				+ Util.squote(hideIf)
				+ ","
				+ Util.squote(defValue)
				+ ","
				+ fkeyRel
				+ ","
				+ Util.squote(desc)
				+ ","
				+ (readonly ? 1 : 0)
				+ ","
				+ (required ? 1 : 0)
				+ ","
				+ (listCol ? 1 : 0)
				+ ","
				+ (addCol ? 1 : 0) + "," + (hidden ? 1 : 0) + "," + 1 + ");";
		this.appDb.execute(s);

	}

	public int getPkById(String srcTbl, String srcCol, String tgtTbl,
			String idVal, String filter, int obj) throws Exception {
		if (idVal != null) {
			String s = null;
			if (obj > 0) {
				s = "select c.rowid from " + tgtTbl
						+ " c, tz_obj t where c.id='" + idVal
						+ "' and t.rowid = c.obj and t.rowid = " + obj;
			} else {
				s = "select rowid from " + tgtTbl + " where id='" + idVal + "'";
			}
			if (filter != null) {
				s += " and " + filter;
			}
			final int ret = this.appDb.getPk(s);
			if (ret < 0) {
				final PendingUpdate pu = new PendingUpdate(srcTbl, srcCol,
						tgtTbl, idVal, obj);
				this.updates.add(pu);
				return pu.pid;
			} else {
				return ret;
			}
		}
		return 0;
	}

	class PendingUpdate {
		public String srcTbl;
		public String srcCol;
		public String tgtTbl;
		public int obj;
		public String idVal;
		public boolean resolved;
		public int pid;

		@Override
		public String toString() {
			return "PendingUpdate [srcTbl=" + this.srcTbl + ", srcCol="
					+ this.srcCol + ", tgtTbl=" + this.tgtTbl + ", idVal="
					+ this.idVal + ", resolved=" + this.resolved + ", pid="
					+ this.pid + "]";
		}

		public PendingUpdate(String srcTbl, String srcCol, String tgtTbl,
				String idVal, int obj) {
			pidx--;
			this.srcTbl = srcTbl;
			this.srcCol = srcCol;
			this.tgtTbl = tgtTbl;
			this.idVal = idVal;
			this.resolved = false;
			this.pid = pidx;
			this.obj = obj;
		}
	}

	class Ret {
		public int pk;
		public String id;
		public boolean pdata;

		public Ret(int pk, String id, boolean pdata) {
			this.pk = pk;
			this.id = id;
			this.pdata = pdata;
		}
	}

	class CmnProp {
		public String id;
		public String name;
		public String desc;

		CmnProp(Element el, String pref) throws Exception {
			this.name = el.getAttributeValue("name");
			this.id = el.getAttributeValue("id");
			if ((this.id == null) || this.id.isEmpty()) {
				this.id = pref + AppCompiler.this.appDb.nextId();
			}
			this.desc = el.getAttributeValue("desc");
		}
	}

	private static void usage() {
		System.out
				.println("Usage : java XMLLoader <app db file> <user db file> <xml file having app schema>");
		System.exit(0);
	}
}
