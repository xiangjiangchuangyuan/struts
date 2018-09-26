package com.xjcy.struts.mapper;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.xjcy.struts.wrapper.EachItem;

/**
 * jsp的对象和跳转页面配置
 * 
 * @author 张梦龙
 *
 */
public class ModelAndView
{
	private static final Logger logger = Logger.getLogger(ModelAndView.class);

	private final Map<String, Object> paras = new HashMap<>();
	private String viewName;
	
	public static ModelAndView view(String viewName){
		ModelAndView mav = new ModelAndView();
		mav.setViewName(viewName);
		return mav;
	}

	public ModelAndView addObject(String key, Object obj)
	{
		paras.put(key, obj);
		return this;
	}

	public <E> ModelAndView addObject(String key, List<E> obj, String sizePara)
	{
		paras.put(key, obj);
		addObject(sizePara, (obj == null ? 0 : obj.size()));
		return this;
	}
	
	public <K, V> ModelAndView addObject(String key, Map<K, V> obj, String sizePara)
	{
		paras.put(key, obj);
		addObject(sizePara, (obj == null ? 0 : obj.size()));
		return this;
	}

	public <T> ModelAndView addObject(String key, EachItem<T> each)
	{
		List<T> objs = each.getList();
		if (objs == null)
			return this;
		for (T t : objs)
		{
			each.doItem(t);
		}
		paras.put(key, objs);
		return this;
	}

	public ModelAndView addObject(Object obj)
	{
		if (obj == null)
			return this;
		Field[] fields = obj.getClass().getDeclaredFields();
		if (fields.length > 0)
		{
			for (Field field : fields)
			{
				if ("serialVersionUID".equals(field.getName()))
					continue;
				try
				{
					field.setAccessible(true);
					addObject(field.getName(), field.get(obj));
					field.setAccessible(false);
					if (logger.isDebugEnabled())
						logger.debug("addObject => " + field.getName() + "[" + field.get(obj) + "]");
				}
				catch (IllegalArgumentException | IllegalAccessException e)
				{
					logger.error("addObject失败", e);
				}
			}
		}
		return this;
	}

	public String getViewName()
	{
		// 不是以jsp结尾，也没有后缀名
		if (!viewName.endsWith(".jsp") && !viewName.contains("."))
			viewName += ".jsp";
		// 前面加/
		if (!viewName.startsWith("/"))
			viewName = "/" + viewName;
		return viewName;
	}

	public Map<String, Object> getParas()
	{
		return paras;
	}

	public ModelAndView removeObject(String key)
	{
		if (paras.containsKey(key))
			paras.remove(key);
		return this;
	}

	public ModelAndView setViewName(String viewName)
	{
		this.viewName = viewName;
		return this;
	}

	public void clear()
	{
		if (paras != null)
			paras.clear();
		viewName = null;
	}

	public void fillRequest(HttpServletRequest request)
	{
		if (paras != null && !paras.isEmpty())
		{
			Set<String> keys = paras.keySet();
			for (String key : keys)
			{
				request.setAttribute(key, paras.get(key));
			}
		}
	}

}
