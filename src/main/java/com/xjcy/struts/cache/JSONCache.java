package com.xjcy.struts.cache;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 缓存bean的字段
 * @author YYDF
 * 2018-05-18
 */
public class JSONCache {
	private static final Map<Class<?>, Field[]> cacheFields = new HashMap<>();

	public static Field[] getDeclaredFields(Class<?> cla) {
		if (cacheFields.containsKey(cla))
			return cacheFields.get(cla);
		Field[] fields = cla.getDeclaredFields();
		cacheFields.put(cla, fields);
		return fields;
	}
}
