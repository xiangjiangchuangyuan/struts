package com.xjcy.struts.web;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

import com.xjcy.struts.context.StrutsContext;
import com.xjcy.struts.context.WebContextUtils;
import com.xjcy.util.STR;
import com.xjcy.util.StringUtils;

public class ContextLoader {

	private static final Logger logger = Logger.getLogger(ContextLoader.class);
	private final static StrutsContext context = new StrutsContext();
	private ServletContext severtContext;

	public ContextLoader(ServletContext arg1) {
		long start = System.currentTimeMillis();
		try {
			this.severtContext = arg1;
			// 扫描所有文件
			context.clear();
			scanPaths(arg1, arg1.getResourcePaths(STR.SLASH_LEFT));
		} catch (Exception e) {
			logger.error("Context load faild", e);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Scan to " + context.getClassSize() + " class files");
			logger.debug("Struts context load with " + (System.currentTimeMillis() - start) + " ms");
		}
	}

	private void scanPaths(ServletContext arg1, Set<String> paths) {
		if (paths != null) {
			for (String path : paths) {
				if (path.endsWith(STR.SLASH_LEFT))
					scanPaths(arg1, arg1.getResourcePaths(path));
				else {
					if (path.endsWith(STR.SUBFIX_CLASS)) {
						Class<?> cla = getClass(path);
						if (cla != null && !cla.isInterface()) {
							bindResource(cla);
							// 如果继承了ActionSupport，则添加到集合中去
							if (WebContextUtils.isAction(cla))
								bindAction(cla);
							else if (WebContextUtils.isInterceptor(cla))
								context.addInterceptor(cla);
							else if (WebContextUtils.isStrutsInit(cla))
								context.addInit(cla);
							// 将class放入集合
							context.addClass(cla);
						}
					}
					else if(path.endsWith(STR.SUBFIX_JSP) || path.endsWith(STR.SUBFIX_JSPX))
						context.addJsp(path);
				}
			}
		}
	}

	private Class<?> getClass(String path) {
		path = path.replace(STR.WEB_CLASS_PATH, STR.EMPTY);
		path = path.replace(STR.SUBFIX_CLASS, STR.EMPTY);
		path = path.replace(STR.SLASH_LEFT, STR.DOT);
		try {
			return Class.forName(path);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	private static void bindResource(Class<?> cla) {
		Field[] fields = cla.getDeclaredFields();
		for (Field field : fields) {
			if (field.getAnnotation(Resource.class) != null) {
				context.addResource(field);
			}
		}
	}

	private static void bindAction(Class<?> cla) {
		String pkg = WebContextUtils.getMappingPath(cla);
		Method[] methods = cla.getMethods();
		String action;
		for (Method method : methods) {
			// 获取request路径
			action = WebContextUtils.getMappingPath(pkg, method);
			if (!StringUtils.isEmpty(action)) {
				if (action.contains("{") && action.contains("}")) {
					List<String> paras = new ArrayList<>();
					int start = 0;
					String para;
					String pattern = action;
					while (true) {
						para = getParameter(action, start);
						if (para == null)
							break;
						start += para.length();
						paras.add(para.replace("{", "").replace("}", ""));
						pattern = pattern.replace(para, "(.*)");
					}
					context.addAction(pattern, method, cla, paras);
				} else
					context.addAction(action, method, cla);
			}
		}
	}

	private static String getParameter(String action, int start) {
		int begin = action.indexOf("{", start);
		if (begin == -1)
			return null;
		int end = action.indexOf("}", begin) + 1;
		return action.substring(begin, end);
	}

	public StrutsContext getContext() {
		return context;
	}

	public void destroy() {
		if (context != null)
			context.destory();
	}

	public void startup() {
		if (context != null)
			context.startup(severtContext);
	}
}
