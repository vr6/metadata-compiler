/*******************************************************************************
 * Copyright (c) 2012 Venkat Reddy
 *******************************************************************************/
package com.worktheme.theme37.compiler.types;

import java.util.ArrayList;
import java.util.List;

public class Exp {
	public static void main(String[] args) {
		fldEval = new FieldEval() {
			@Override
			public int eval(String s) {
				if (s.equals("one"))
					return 1;
				else if (s.equals("two"))
					return 2;
				else if (s.equals("five"))
					return 5;

				return 0;
			}
		};
		final String exp = "2 + 4* (five- (2*(6+two) + 6)) -5";
		System.out.println("res=" + doSums(exp));
	}

	private static FieldEval fldEval;

	private static int doSums(String exp) {
		System.out.println("doSums:\t\t'" + exp + "'");
		if (exp.startsWith("(") && exp.endsWith(")"))
			exp = exp.substring(1, exp.length() - 1);
		int ret = 0;
		int k1 = 0;
		boolean add = true;
		final List<Summand> sums = new ArrayList<Summand>();
		for (int i = 0; i < exp.length(); i++) {
			final char c = exp.charAt(i);
			if (c == '(') {
				final int k = getBlock(exp, i);
				i = k;
			} else if ((c == '+') || (c == '-')) {
				sums.add(new Summand(exp.substring(k1, i).trim(), add));
				k1 = i + 1;
				add = c == '+';
			}
		}
		sums.add(new Summand(exp.substring(k1).trim(), add));

		for (final Summand s : sums) {
			int v = 0;
			if (isExp(s.factor)) {
				v = doFactors(s.factor);
			} else if (isNumber(s.factor)) {
				v = Integer.parseInt(s.factor);
			} else {
				v = fldEval.eval(s.factor);
			}
			ret = s.add ? ret + v : ret - v;
		}
		System.out.println("doSums:returning=" + ret);
		return ret;
	}

	private static int doFactors(String exp) {
		System.out.println("dofactors:\t'" + exp + "'");
		if (exp.startsWith("(") && exp.endsWith(")")) {
			return doSums(exp.substring(1, exp.length() - 1));
		}
		int ret = 1;
		int k1 = 0;
		boolean multi = true;
		final List<Factor> factors = new ArrayList<Factor>();
		for (int i = 0; i < exp.length(); i++) {
			final char c = exp.charAt(i);
			if (c == '(') {
				final int k = getBlock(exp, i);
				i = k;
			} else if ((c == '*') || (c == '/')) {
				factors.add(new Factor(exp.substring(k1, i).trim(), multi));
				k1 = i + 1;
				multi = c == '*';
			}
		}
		factors.add(new Factor(exp.substring(k1).trim(), multi));

		for (final Factor s : factors) {
			int v = 0;
			if (isExp(s.sum)) {
				v = doFactors(s.sum);
			} else if (isNumber(s.sum)) {
				v = Integer.parseInt(s.sum);
			} else {
				v = fldEval.eval(s.sum);
			}
			ret = s.multi ? ret * v : ret / v;
		}
		System.out.println("dofactors:returning=" + ret);
		return ret;
	}

	public static boolean isExp(String val) {
		return val.matches("^.*[+\\-*/()]+.*$");
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

	private static int getBlock(String exp, int k) {
		int t = 0;
		for (int i = k + 1; i < exp.length(); i++) {
			final char c = exp.charAt(i);
			if (c == '(') {
				t++;
			} else if (c == ')') {
				if (t > 0)
					t--;
				else
					return i;
			}
		}
		System.out.println("ERROR: matching bracket not found.");
		return k;
	}
}

class Summand {
	String factor;
	boolean add;

	Summand(String s, boolean b) {
		this.factor = s;
		this.add = b;
	}
}

class Factor {
	String sum;
	boolean multi;

	Factor(String s, boolean b) {
		this.sum = s;
		this.multi = b;
	}
}
