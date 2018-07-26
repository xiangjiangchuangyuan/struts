package com.xjcy.struts.context;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.xjcy.struts.ActionInterceptor;
import com.xjcy.struts.StrutsInit;
import com.xjcy.struts.annotation.Order;
import com.xjcy.struts.mapper.ActionMapper;
import com.xjcy.struts.mapper.SpringBean;
import com.xjcy.struts.wrapper.JSPWrapper;
import com.xjcy.util.STR;
import com.xjcy.util.StringUtils;

public class StrutsContext {
	private static final Logger logger = Logger.getLogger(StrutsContext.class);

	private static final List<Class<?>> classlist = new ArrayList<>();
	private static final List<String> jspList = new ArrayList<>();
	private static final List<StrutsInit> initList = new ArrayList<>();
	private static final List<Class<?>> interceptors = new ArrayList<>();
	private static final Map<String, ActionMapper> actionMap = new HashMap<>();
	private static final Map<String, ActionMapper> patternActionMap = new HashMap<>();
	private static final Map<Class<?>, List<SpringBean>> springMap = new HashMap<>();

	public StrutsContext(ServletContext servlet, JSPWrapper jspWrapper) {
		scanPaths(servlet, servlet.getResourcePaths(STR.SLASH_LEFT));
		startup(servlet, jspWrapper);
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
								addInterceptor(cla);
							else if (WebContextUtils.isStrutsInit(cla))
								addInit(cla);
							// 将class放入集合
							addClass(cla);
						}
					} else if (path.endsWith(STR.SUBFIX_JSP) || path.endsWith(STR.SUBFIX_JSPX))
						addJsp(path);
				}
			}
		}
	}

	private static Class<?> getClass(String path) {
		path = path.replace(STR.WEB_CLASS_PATH, STR.EMPTY);
		path = path.replace(STR.SUBFIX_CLASS, STR.EMPTY);
		path = path.replace(STR.SLASH_LEFT, STR.DOT);
		try {
			return Class.forName(path);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	private void bindResource(Class<?> cla) {
		Field[] fields = cla.getDeclaredFields();
		for (Field field : fields) {
			if (field.getAnnotation(Resource.class) != null) {
				if (springMap.containsKey(cla))
					springMap.get(cla).add(new SpringBean(field));
				else {
					List<SpringBean> beans = new ArrayList<>();
					beans.add(new SpringBean(field));
					springMap.put(cla, beans);
				}
			}
		}
	}

	private void bindAction(Class<?> cla) {
		String pkg = WebContextUtils.getMappingPath(cla);
		Method[] methods = cla.getMethods();
		String action;
		for (Method method : methods) {
			// 获取request路径
			action = WebContextUtils.getMappingPath(pkg, method);
			if (StringUtils.isNotBlank(action)) {
				if (action.contains("{") && action.contains("}")) {
					List<String> paras = new ArrayList<>();
					String para;
					String pattern = action;
					while ((para = getParameter(pattern)) != null) {
						paras.add(para.replace("{", "").replace("}", ""));
						pattern = pattern.replace(para, "(.*)");
					}
					addAction(pattern, method, paras);
				} else
					addAction(action, method);
			}
		}
	}

	private static String getParameter(String action) {
		int begin = action.indexOf("{");
		if (begin == -1)
			return null;
		int end = action.indexOf("}", begin) + 1;
		return action.substring(begin, end);
	}

	public void startup(ServletContext sc, JSPWrapper jspWrapper) {
		// 查找@Resource的实现类
		findResource();
		if (actionMap.size() > 0) {
			mappingAction(sc.getFilterRegistration("StrutsFilter"));
		}
		if (interceptors.size() > 1) {
			sortInterceptor();
		}
		if (initList.size() > 0) {
			startInit(sc);
		}
		// 判断线上环境，执行预编译
		if (jspList.size() > 0 && WebContextUtils.isLinuxOS()) {
			jspWrapper.execute(jspList);
		}
	}

	private void startInit(ServletContext sc) {
		for (StrutsInit init : initList) {
			try {
				init.init(sc);
			} catch (Exception e) {
				logger.error("The " + init.getClass() + " init faild", e);
			}
		}
	}

	private void sortInterceptor() {
		// 按Order注解排序
		Collections.sort(interceptors, new Comparator<Class<?>>() {
			@Override
			public int compare(Class<?> arg0, Class<?> arg1) {
				Integer o1 = 0, o2 = 0;
				Order order0 = arg0.getAnnotation(Order.class);
				if (order0 != null)
					o1 = order0.value();
				Order order1 = arg1.getAnnotation(Order.class);
				if (order1 != null)
					o2 = order1.value();
				return o1.compareTo(o2);
			}
		});
	}

	private void findResource() {
		Set<Class<?>> list = springMap.keySet();
		List<SpringBean> beanList;
		for (Class<?> cla : list) {
			beanList = springMap.get(cla);
			for (SpringBean bean : beanList) {
				for (int i = 0; i < classlist.size(); i++) {
					if (bean.isTarget(classlist.get(i))) {
						bean.setTargetClass(classlist.get(i));
						break;
					}
				}
			}
		}
		classlist.clear();
	}

	private void mappingAction(FilterRegistration filterRegistration) {
		EnumSet<DispatcherType> dispatcherTypes = EnumSet.allOf(DispatcherType.class);
		dispatcherTypes.add(DispatcherType.REQUEST);
		dispatcherTypes.add(DispatcherType.FORWARD);
		// 如果没有通配符的链接
		if (patternActionMap.isEmpty()) {
			Set<String> actions = actionMap.keySet();
			for (String action : actions) {
				filterRegistration.addMappingForUrlPatterns(dispatcherTypes, true, action);
			}
		} else {
			filterRegistration.addMappingForUrlPatterns(dispatcherTypes, true, "/*");
		}
	}

	public void destory() {
		classlist.clear();
		initList.clear();
		interceptors.clear();
		jspList.clear();

		// 清除解析好的Action和Bean
		actionMap.clear();
		patternActionMap.clear();
		springMap.clear();
		if (!initList.isEmpty()) {
			for (StrutsInit init : initList) {
				try {
					init.destroy();
				} catch (Exception e) {
					logger.error("The " + init.getClass() + " destroy faild", e);
				}
			}
		}
	}

	public void addInterceptor(Class<?> cla) {
		interceptors.add(cla);
	}

	public void addInit(Class<?> cla) {
		try {
			initList.add((StrutsInit) cla.newInstance());
		} catch (InstantiationException | IllegalAccessException e) {
			logger.error("Instance init faild", e);
		}
	}

	public void addClass(Class<?> cla) {
		classlist.add(cla);
	}

	public void addAction(String action, Method method) {
		ActionMapper actionMapper = actionMap.put(action, new ActionMapper(method));
		if (actionMapper != null)
			logger.error("Action " + action + " exist, override with " + method.getName());
	}

	public void addAction(String pattern, Method method, List<String> paras) {
		ActionMapper actionMapper = patternActionMap.put(pattern, new ActionMapper(method, paras));
		if (actionMapper != null)
			logger.error("Action pattern " + pattern + " exist, override with " + method.getName());
	}

	public void addJsp(String path) {
		jspList.add(path);
	}

	public ActionMapper getAction(String servletPath) {
		ActionMapper mapper = actionMap.get(servletPath);
		// 如果没有找到，则遍历所有正则路径
		if (mapper == null && !patternActionMap.isEmpty()) {
			Set<String> patterns = patternActionMap.keySet();
			for (String pattern : patterns) {
				Matcher match = Pattern.compile(pattern).matcher(servletPath);
				while (match.find()) {
					mapper = patternActionMap.get(pattern);
					mapper.setParasValue(match);
					break;
				}
			}
		}
		return mapper;
	}

	public boolean checkInterceptors(HttpServletRequest request, HttpServletResponse response) {
		if (interceptors.isEmpty())
			return true;
		ActionInterceptor interceptor;
		for (Class<?> cla : interceptors) {
			interceptor = (ActionInterceptor) getBean(cla);
			logger.debug("Check " + cla.getSimpleName());
			if (!interceptor.intercept(request, response))
				return false;
		}
		return true;
	}

	public static synchronized Object getBean(Class<?> controller) {
		try {
			Object obj = controller.newInstance();
			annotationInject(obj, springMap.get(controller));
			return obj;
		} catch (InstantiationException | IllegalAccessException e) {
			logger.error("Create '" + controller.getName() + "' bean faild", e);
		}
		return null;
	}

	private static void annotationInject(Object obj, List<SpringBean> beans) {
		if (beans != null && !beans.isEmpty()) {
			for (SpringBean springBean : beans) {
				springBean.setTargetValue(obj, getBean(springBean.getTargetClass()));
			}
		}
	}

	public int actionSize() {
		return actionMap.size() + patternActionMap.size();
	}

	public int beanSize() {
		return springMap.size();
	}

	public int interceptorSize() {
		return interceptors.size();
	}
}
