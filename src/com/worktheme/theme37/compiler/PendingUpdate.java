/*******************************************************************************
 * Copyright (c) 2012 Venkat Reddy
 *******************************************************************************/
package com.worktheme.theme37.compiler;

import java.util.ArrayList;
import java.util.List;

public class PendingUpdate {
	private static int pidx = -1;
	private static List<PendingUpdate> updates = new ArrayList<PendingUpdate>();
	public String srcTbl;
	public String srcCol;
	public String tgtTbl;
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
			String idVal) {
		pidx--;
		this.srcTbl = srcTbl;
		this.srcCol = srcCol;
		this.tgtTbl = tgtTbl;
		this.idVal = idVal;
		this.resolved = false;
		this.pid = pidx;
	}

	public static void add(PendingUpdate pu) {
		updates.add(pu);
	}

	public static List<PendingUpdate> getUpdates() {
		return updates;
	}
}
