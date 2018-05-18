package com.xjcy.struts.cache;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

/**
 * 缓存Controller中的Resource注解类
 * @author YYDF
 * 2018-05-18
 */
public class FieldCache {
	private static final Map<Class<?>, List<Field>> fields = new HashMap<>();

	public static List<Field> getResourceFields(Class<?> cla) {
		if(fields.containsKey(cla))
			return fields.get(cla);
		Field[] fieldArr =  cla.getDeclaredFields();
		List<Field> fieldList = new ArrayList<>();
		for (Field field : fieldArr) {
			if(field.getAnnotation(Resource.class) != null)
				fieldList.add(field);
		}
		fields.put(cla, fieldList);
		return fieldList;
	}
}
