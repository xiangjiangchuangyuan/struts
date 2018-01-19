package com.xjcy.struts.wrapper;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * map转换为JSON
 * @author YYDF
 * 2018-01-15
 */
public class ObjectWrapper
{
	private static final Logger logger = Logger.getLogger(ObjectWrapper.class);

	static final String STR_VERSION_UID = "serialVersionUID";
	static final String STR_EMPTY = "";
	static final String STR_SLASH = "\"";
	static final String STR_OBJECT_LEFT = "{";
	static final String STR_SLASH_OBJECT = "\":{";
	static final String STR_SLASH_ARRAY = "\":[";
	static final Map<String, Field[]> cacheFields = new HashMap<>();

	public String write(Map<String, Object> jsonMap)
	{
		long start = System.nanoTime();
		StringBuilder json = new StringBuilder();
		try
		{
			appendMap(null, jsonMap, json);
			logger.debug("JSON build in " + (System.nanoTime() - start) + "ns");
		}
		catch (Exception e)
		{
			logger.error("转换JSON失败", e);
		}
		return json.toString();
	}

	private static void appendObj(String key, Object value, StringBuilder json)
	{
		if (value == null || STR_VERSION_UID.equals(key) || value instanceof Logger)
		{
			// do noting
		}
		else if (value instanceof String || value instanceof CharSequence)
		{
			if (value.toString().startsWith("{") 
					|| value.toString().startsWith("["))
				json.append(STR_SLASH).append(key).append("\":").append(value).append(",");
			else 
				json.append(STR_SLASH).append(key).append("\":\"").append(value).append("\",");
		}
		else if (value instanceof Integer 
				|| value instanceof Boolean 
				|| value instanceof Double 
				|| value instanceof Long)
			json.append(STR_SLASH).append(key).append("\":").append(value).append(",");
		else if (value instanceof Map)
			appendMap(key, (Map<?, ?>) value, json);
		else if (value instanceof Object[] 
				|| value instanceof int[] 
				|| value instanceof long[]
				|| value instanceof byte[] 
				|| value instanceof char[])
			appendArray(key, value, json);
		else if (value instanceof Collection)
			appendList(key, (Collection<?>) value, json);
		else // 解析bean
			appendBean(key, value, json);
	}

	private static void appendBean(String key, Object value, StringBuilder json)
	{
		if (key == null)
			json.append(STR_OBJECT_LEFT);
		else json.append(STR_SLASH).append(key).append(STR_SLASH_OBJECT);
		Field[] fields = getDeclaredFields(value);
		int num = 0;
		Object obj2;
		for (Field field : fields)
		{
			obj2 = getObject(field, value);
			if (obj2 != null)
			{
				appendObj(field.getName(), obj2, json);
				num++;
			}
		}
		if (num > 0)
			json.delete(json.length() - 1, json.length());
		json.append("},");
	}

	/**
	 * 处理Map
	 * 
	 * @param key
	 * @param value
	 * @param json
	 * @return
	 */
	private static void appendMap(String key, Map<?, ?> value, StringBuilder json)
	{
		if (key == null)
		{
			json.append("{");
			Set<?> keys = value.keySet();
			for (Object obj : keys)
			{
				appendObj(obj.toString(), value.get(obj), json);
			}
			if (!value.isEmpty())
				json.delete(json.length() - 1, json.length());
			json.append("}");
		}
		else
		{
			json.append(STR_SLASH).append(key).append(STR_SLASH_OBJECT);
			Set<?> keys = value.keySet();
			for (Object obj : keys)
			{
				appendObj(obj.toString(), value.get(obj), json);
			}
			if (!value.isEmpty())
				json.delete(json.length() - 1, json.length());
			json.append("},");
		}
	}

	/***
	 * 处理List集合
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	private static void appendList(String key, Collection<?> value, StringBuilder json)
	{
		json.append(STR_SLASH).append(key).append(STR_SLASH_ARRAY);
		for (Object obj : value)
		{
			if (obj instanceof String || obj instanceof CharSequence)
			{
				json.append(STR_SLASH).append(obj).append("\",");
			}
			else if (obj instanceof Integer || obj instanceof Boolean || obj instanceof Long)
			{
				json.append(obj).append(",");
			}
			else
			{
				appendBean(null, obj, json);
			}
		}
		if (value.size() > 0)
			json.delete(json.length() - 1, json.length());
		json.append("],");
	}

	private static synchronized Object getObject(Field field, Object obj)
	{
		try
		{
			field.setAccessible(true);
			return field.get(obj);
		}
		catch (IllegalArgumentException | IllegalAccessException e)
		{
			return null;
		}
		finally
		{
			field.setAccessible(false);
		}
	}

	private static synchronized Field[] getDeclaredFields(Object obj)
	{
		String cacheKey = obj.getClass().getName();
		if (cacheFields.containsKey(cacheKey))
			return cacheFields.get(cacheKey);
		Field[] fields = obj.getClass().getDeclaredFields();
		cacheFields.put(cacheKey, fields);
		return fields;
	}

	/**
	 * 处理数组
	 * 
	 * @param key
	 * @param array
	 * @return
	 */
	private static void appendArray(String key, Object array, StringBuilder json)
	{
		json.append(STR_SLASH).append(key).append(STR_SLASH_ARRAY);
		int len = Array.getLength(array);
		Object obj;
		for (int i = 0; i < len; i++)
		{
			obj = Array.get(array, i);
			if (obj instanceof Integer || obj instanceof Long)
				json.append(obj);
			else 
				json.append(STR_SLASH).append(obj).append(STR_SLASH);
			json.append(",");
		}
		if (len > 0)
			json.delete(json.length() - 1, json.length());
		json.append("],");
	}
}
