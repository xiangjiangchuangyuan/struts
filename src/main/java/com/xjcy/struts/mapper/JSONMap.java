package com.xjcy.struts.mapper;

import java.util.HashMap;
import java.util.Map;

import com.xjcy.struts.wrapper.JSONWrapper;

/**
 * JSON对象处理类
 * 
 * @author YYDF
 *
 */
public final class JSONMap
{
	private final Map<String, Object> jsonMap = new HashMap<>();
	private final JSONWrapper wrapper = new JSONWrapper();
	private String json;
	
	public JSONMap()
	{
		this(null);
	}

	public JSONMap(String json)
	{
		this.json = json;
	}

	public JSONMap put(String key, Object val)
	{
		jsonMap.put(key, val);
		return this;
	}

	public JSONMap putAll(Map<String, Object> map)
	{
		jsonMap.putAll(map);
		return this;
	}

	public Map<String, Object> getMap()
	{
		return jsonMap;
	}

	public static JSONMap success()
	{
		return new JSONMap().put("success", true).put("errcode", 0).put("errmsg", "ok");
	}

	public static JSONMap error(int errcode, String errmsg)
	{
		return new JSONMap().put("success", false).put("errcode", errcode).put("errmsg", errmsg);
	}

	@Override
	public String toString()
	{
		if (json == null) 
			return wrapper.write(jsonMap);// 转换成JSON
		return json;
	}

}
