package com.xjcy.struts.mapper;

import java.util.HashMap;
import java.util.Map;

import org.apache.jasper.runtime.HttpJspBase;

/**
 * 缓存jsp预编译的class文件
 * @author YYDF
 * 2018-04-11
 */
public class JspCache {
	private static final Map<String, HttpJspBase> jspServlets = new HashMap<>();

	public static HttpJspBase getServlet(String jspUri) {
		return jspServlets.get(jspUri);
	}

	public static void put(String jspUri, HttpJspBase servlet) {
		jspServlets.put(jspUri, servlet);
	}
}
