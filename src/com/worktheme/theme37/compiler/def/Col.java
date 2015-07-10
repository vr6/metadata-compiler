/*******************************************************************************
 * Copyright (c) 2012 Venkat Reddy
 *******************************************************************************/
package com.worktheme.theme37.compiler.def;

import org.jdom.Element;

public interface Col {

	public abstract String getVal(Element el, int parent, String parentTag)
			throws Exception;

	public abstract String getId();

	public abstract String getParentTag();

}
