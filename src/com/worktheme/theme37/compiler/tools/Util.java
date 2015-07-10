/*******************************************************************************
 * Copyright (c) 2012 Venkat Reddy
 *******************************************************************************/
package com.worktheme.theme37.compiler.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Arrays;

public class Util {
	public static int[] findPrimes(int max) {
		final int n = max / 2;
		final boolean[] isPrime = new boolean[max];

		Arrays.fill(isPrime, true);

		for (int i = 1; i < n; i++) {
			for (int j = i; j <= ((n - i) / ((2 * i) + 1)); j++) {
				isPrime[i + j + (2 * i * j)] = false;
			}
		}

		final int[] primes = new int[max];
		int found = 0;
		if (max > 2) {
			primes[found++] = 2;
		}
		for (int i = 1; i < n; i++) {
			if (isPrime[i]) {
				primes[found++] = (i * 2) + 1;
			}
		}
		return Arrays.copyOf(primes, found);
	}

	public static String squote(Object val) {
		if (val == null)
			return "null";
		if (val instanceof String)
			return "'" + val.toString().replace("'", "''") + "'";
		return val.toString();
	}

	public static boolean isInteger(String val) {
		final char[] a = val.trim().toCharArray();
		for (final char c : a) {
			if (!Character.isDigit(c))
				return false;
		}
		return true;
	}

	public static boolean isDecimal(String val) {
		final char[] a = val.trim().toCharArray();
		boolean dot = false;
		for (final char c : a) {
			if (!dot && (c == '.')) {
				dot = true;
			} else if (!Character.isDigit(c))
				return false;
		}
		return true;
	}

	public static boolean isNumber(String val) {
		return val.matches("^\\d+$");
	}

	public static boolean isNumeric(String val) {
		return val.matches("^[-+]?\\d+(\\.\\d+)?$");
	}

	public static boolean isNumberWith2Decimals(String val) {
		return val.matches("^\\d+\\.\\d{2}$");
	}

	public static String getAn(String name) {
		if (name.startsWith("A") || name.startsWith("a")
				|| name.startsWith("E") || name.startsWith("e")
				|| name.startsWith("I") || name.startsWith("i")
				|| name.startsWith("O") || name.startsWith("o")
				|| name.startsWith("U") || name.startsWith("u"))
			return "an ";
		return "a ";
	}

	public static String getPlural(String name) {
		if (name == null)
			return null;
		if (name.endsWith("is"))
			return name.substring(0, name.length() - 2) + "es";
		else if (name.endsWith("ch") || name.endsWith("x")
				|| name.endsWith("s") || name.endsWith("o"))
			return name + "es";
		else if (name.endsWith("ta"))
			return name;
		else if (name.endsWith("fe"))
			return name.substring(0, name.length() - 2) + "ves";
		else if (name.endsWith("ff"))
			return name + "s";
		else if (name.endsWith("f"))
			return name.substring(0, name.length() - 1) + "ves";
		else if (name.endsWith("us"))
			return name.substring(0, name.length() - 2) + "i";
		else if (name.endsWith("y") && !name.endsWith("ay")
				&& !name.endsWith("oy"))
			return name.substring(0, name.length() - 1) + "ies";
		else if (name.endsWith("non") || name.endsWith("um"))
			return name.substring(0, name.length() - 2) + "a";
		return name + "s";
	}

	public static void copyFile(File sourceFile, File destFile)
			throws IOException {
		if (!destFile.exists()) {
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;

		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} finally {
			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
		}
	}

}
