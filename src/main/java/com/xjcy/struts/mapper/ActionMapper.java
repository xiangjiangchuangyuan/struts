package com.xjcy.struts.mapper;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.xjcy.struts.ActionSupport;
import com.xjcy.struts.context.WebContextUtils;

/**
 * Action处理类
 * 
 * @author YYDF
 *
 */
public class ActionMapper
{
	private static final Logger logger = Logger.getLogger(ActionMapper.class);

	private final Method actionMethod;
	private final Class<?> returnType;
	private final List<String> paras;
	private final Map<String, String> paraValues = new HashMap<>();
	private final boolean redisCache;
	private final int cacheSeconds;
	private ActionSupport cacheBean;

	public ActionMapper(Method method)
	{
		this(method, null);
	}

	public ActionMapper(Method method, List<String> paras) {
		this.actionMethod = method;
		this.cacheSeconds = WebContextUtils.getRedisCache(method);
		this.redisCache = cacheSeconds > 0;
		this.returnType = method.getReturnType();
		this.paras = paras;
	}

	public ActionSupport getBean() {
		return this.cacheBean;
	}

	public Object invoke(HttpServletRequest request, HttpServletResponse response) {
		Object resultObj = null;
		try {
			this.cacheBean.setRequest(request);
			this.cacheBean.setResponse(response);
			resultObj = actionMethod.invoke(this.cacheBean);
		} catch (Exception e) {
			logger.error("Action call " + actionMethod.getName() + " faild", e);
		} finally {
			this.cacheBean.destory();
			logger.debug("The action has been destroyed");
		}
		return resultObj;
	}

	public boolean isPatternAction()
	{
		return this.paras != null;
	}

	public void setParasValue(Matcher match)
	{
		paraValues.clear();
		int num = 1;
		for (String para : paras)
		{
			paraValues.put(para, match.group(num));
			num++;
		}
	}

	public Class<?> getReturnType()
	{
		return this.returnType;
	}

	public void fillRequest(HttpServletRequest request)
	{
		Set<String> keys = paraValues.keySet();
		for (String key : keys)
		{
			request.setAttribute(key, paraValues.get(key));
		}
	}

	public boolean getRedisCache()
	{
		return this.redisCache;
	}
	
	public int getCacheSeconds()
	{
		return this.cacheSeconds;
	}

	public String getName() {
		return actionMethod.getName();
	}

	public Class<?> getController() {
		return actionMethod.getDeclaringClass();
	}

	public void cacheBean(Object bean) {
		this.cacheBean = (ActionSupport) bean;
	}
}
