package com.xjcy.struts.context;

import java.io.File;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;

import com.xjcy.struts.ActionInterceptor;
import com.xjcy.struts.ActionSupport;
import com.xjcy.struts.StrutsInit;
import com.xjcy.struts.annotation.RequestMapping;
import com.xjcy.struts.annotation.RequestMapping.HttpMethod;
import com.xjcy.util.LoggerUtils;
import com.xjcy.util.STR;
import com.xjcy.util.StringUtils;

public class WebContextUtils {
	private static final LoggerUtils logger = LoggerUtils.from(WebContextUtils.class);

	static Boolean isLinux;
	private static String basePath;

	public static boolean isAction(Class<?> beanClass) {
		// 判断是不是controller
		return ActionSupport.class.isAssignableFrom(beanClass);
	}

	public static boolean isInterceptor(Class<?> beanClass) {
		// 判断是不是拦截器
		return ActionInterceptor.class.isAssignableFrom(beanClass);
	}

	/**
	 * 判断浏览器是否支持GZIP
	 * 
	 * @param request
	 * @return
	 */
	public static boolean isGZipEncoding(HttpServletRequest request) {
		boolean flag = false;
		String encoding = request.getHeader(STR.HEADER_ACCEPT_ENCODING);
		if (encoding != null && encoding.indexOf(STR.ENCODING_GZIP) > -1)
			flag = true;
		if (logger.isDebugEnabled())
			logger.debug("Support gzip => " + flag);
		return flag;
	}

	public static boolean isStrutsInit(Class<?> cla) {
		// 判断Struts2Init是不是cla的父类
		return StrutsInit.class.isAssignableFrom(cla);
	}

	public static String getMappingPath(Class<?> cla) {
		RequestMapping rm = cla.getAnnotation(RequestMapping.class);
		return rm != null ? rm.value() : STR.EMPTY;
	}

	public static String getMappingPath(String pkg, Method method) {
		RequestMapping rm = method.getAnnotation(RequestMapping.class);
		if (rm != null) {
			String path = rm.value();
			if (path.startsWith("~"))
				return path.substring(1);
			return pkg + path;
		}
		return null;
	}
	
	public static HttpMethod getHttpMethod(Method method) {
		RequestMapping rm = method.getAnnotation(RequestMapping.class);
		return rm != null ? rm.method() : HttpMethod.GET;
	}

	public static boolean isLinuxOS() {
		if (isLinux == null) {
			String os = System.getProperty("os.name");
			isLinux = (os != null && os.toLowerCase().indexOf("linux") > -1);
		}
		return isLinux;
	}

	public static File getJspServletFile(File output, String className) {
		return new File(output, "/org/apache/jsp/" + className);
	}

	public static String getBasePath(HttpServletRequest request) {
		if (StringUtils.isEmpty(basePath)) {
			if (isLinuxOS())
				basePath = "/";
			else {
				int port = request.getServerPort();
				basePath = request.getScheme() + "://" + request.getServerName() + (port == 80 ? "" : ":" + port)
						+ request.getContextPath() + "/";
			}
		}
		return basePath;
	}
}
