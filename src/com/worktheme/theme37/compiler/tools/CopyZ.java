/*******************************************************************************
 * Copyright (c) 2012 Venkat Reddy
 *******************************************************************************/
package com.worktheme.theme37.compiler.tools;

import java.io.BufferedReader;
import java.io.FileReader;

import com.worktheme.theme37.compiler.Database;

public class CopyZ {
	public static void main(String args[]) throws Exception {
		final Database z = new Database("testz.sqlite");
		final BufferedReader br = new BufferedReader(new FileReader("z6.sql"));
		final StringBuffer buf = new StringBuffer();
		for (String s = br.readLine(); s != null; s = br.readLine()) {
			buf.append(s + "\n");
		}
		br.close();
		z.execute(buf.toString());
		z.close();
	}
}
