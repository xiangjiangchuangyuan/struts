package com.xjcy.struts.context;

import java.io.File;
import java.lang.reflect.Method;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.xjcy.struts.ActionInterceptor;
import com.xjcy.struts.ActionSupport;
import com.xjcy.struts.StrutsInit;
import com.xjcy.struts.annotation.RedisCache;
import com.xjcy.struts.annotation.RequestMapping;
import com.xjcy.struts.web.ContextLoader;
import com.xjcy.util.STR;

public class WebContextUtils
{
	private static final Logger logger = Logger.getLogger(WebContextUtils.class);

	static Boolean isLinux;

	public static StrutsContext getWebApplicationContext(ServletContext servletContext)
	{
		Object obj = servletContext.getAttribute(STR.STRUTS_IS_LOAD);
		// 如果没有加载context
		if (obj == null || (boolean) obj == false)
		{
			ContextLoader loader = new ContextLoader(servletContext);
			loader.startup();
			return loader.getContext();
		}
		return (StrutsContext) servletContext.getAttribute(STR.STRUTS_CONTEXT);
	}

	public static boolean isAction(Class<?> beanClass)
	{
		// 判断ActionSupport是不是beanClass的父类
		return ActionSupport.class.isAssignableFrom(beanClass);
	}

	public static boolean isInterceptor(Class<?> beanClass)
	{
		// 判断ActionSupport是不是beanClass的父类
		return ActionInterceptor.class.isAssignableFrom(beanClass);
	}

	/**
	 * 判断浏览器是否支持GZIP
	 * 
	 * @param request
	 * @return
	 */
	public static boolean isGZipEncoding(HttpServletRequest request)
	{
		boolean flag = false;
		String encoding = request.getHeader(STR.HEADER_ACCEPT_ENCODING);
		if (encoding != null && encoding.indexOf(STR.ENCODING_GZIP) > -1)
			flag = true;
		if (logger.isDebugEnabled())
			logger.debug("Support gzip => " + flag);
		return flag;
	}

	public static boolean isStrutsInit(Class<?> cla)
	{
		// 判断Struts2Init是不是cla的父类
		return StrutsInit.class.isAssignableFrom(cla);
	}

	public static String getMappingPath(Class<?> cla)
	{
		RequestMapping rm = cla.getAnnotation(RequestMapping.class);
		if (rm != null)
			return rm.value();
		return "";
	}

	public static String getMappingPath(String pkg, Method method)
	{
		RequestMapping rm = method.getAnnotation(RequestMapping.class);
		if (rm != null)
		{
			String path = rm.value();
			if (rm.value().startsWith("~"))
				return path.substring(1, path.length());
			return pkg + path;
		}
		return null;
	}
	
	public static int getRedisCache(Method method)
	{
		RedisCache cache = method.getAnnotation(RedisCache.class);
		if(cache == null)
			return -1;
		return cache.value();
	}

	public static boolean isLinuxOS()
	{
		if (isLinux == null)
		{
			String os = System.getProperty("os.name");
			isLinux = (os != null && os.toLowerCase().indexOf("linux") > -1);
		}
		return isLinux;
	}

	public static File getJspServletFile(File output, String className)
	{
		return new File(output, "/org/apache/jsp/" + className);
	}
}
