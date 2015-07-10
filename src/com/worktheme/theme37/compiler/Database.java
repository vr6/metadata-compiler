/*******************************************************************************
 * Copyright (c) 2012 Venkat Reddy
 *******************************************************************************/
package com.worktheme.theme37.compiler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Database {
	static {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (final ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	String dbPath;
	Connection conn;
	Statement stmt;

	public Database(String path) throws Exception {
		this.dbPath = path;
		this.conn = DriverManager.getConnection("jdbc:sqlite:" + this.dbPath);
		this.conn.setAutoCommit(true);
		this.stmt = this.conn.createStatement();
	}

	public String getFileName() {
		return this.dbPath;
	}

	public int insert(String tbl, String sql) throws Exception {
		int ret = 0;
		this.stmt.execute(sql);
		sql = "select max(rowid) 'max' from " + tbl + ";";
		final ResultSet rs = this.stmt.executeQuery(sql);
		if (rs.next()) {
			ret = rs.getInt("max");
		}
		rs.close();
		return ret;
	}

	public void execute(String sql) throws Exception {
		final String[] qs = sql.split("\n");
		for (final String q : qs) {
			if (!q.isEmpty())
				this.stmt.execute(q);
		}
	}

	public List<Object[]> query(String sql, int ccount) throws Exception {
		final List<Object[]> res = new ArrayList<Object[]>();
		final ResultSet rs = this.stmt.executeQuery(sql);
		while (rs.next()) {
			final Object[] row = new Object[ccount];
			for (int i = 0; i < ccount; i++) {
				row[i] = (rs.getObject(i + 1));
			}
			res.add(row);
		}
		rs.close();
		return res;
	}

	public int nextId() throws Exception {
		String sql = "select nxt from tz_nxtid;";
		int ret = -1;
		final ResultSet rs = this.stmt.executeQuery(sql);
		if (rs.next()) {
			ret = rs.getInt("nxt");
		}
		rs.close();
		sql = "update tz_nxtid set nxt = " + (ret + 1) + ";";
		this.stmt.execute(sql);
		return ret;
	}

	public int getPk(String sql) throws Exception {
		int ret = -1;
		final ResultSet rs = this.stmt.executeQuery(sql);
		if (rs.next()) {
			ret = rs.getInt(1);
		}
		rs.close();
		return ret;
	}

	public String getStrVal(String sql) throws Exception {
		String ret = null;
		final ResultSet rs = this.stmt.executeQuery(sql);
		if (rs.next()) {
			ret = rs.getString(1);
		}
		rs.close();
		return ret;
	}

	public void close() throws Exception {
		if (this.stmt != null)
			this.stmt.close();
		if ((this.conn != null) && !this.conn.isClosed())
			this.conn.close();
	}
}
