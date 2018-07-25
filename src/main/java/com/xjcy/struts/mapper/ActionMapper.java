package com.xjcy.struts.mapper;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.xjcy.struts.ActionSupport;
import com.xjcy.struts.context.StrutsContext;
import com.xjcy.struts.context.WebContextUtils;
import com.xjcy.struts.wrapper.ResponseWrapper;

/**
 * Action处理类
 * 
 * @author YYDF
 *
 */
public class ActionMapper {
	private static final Logger logger = Logger.getLogger(ActionMapper.class);

	private final Method actionMethod;
	private List<String> paras;
	private Map<String, String> paraValues;
	private Class<?> declaringClass;
	private Class<?>[] parameterTypes;
	private int length;

	public ActionMapper(Method method) {
		this(method, null);
	}

	public ActionMapper(Method method, List<String> paras) {
		this.actionMethod = method;
		this.declaringClass = method.getDeclaringClass();
		this.parameterTypes = method.getParameterTypes();
		this.length = this.parameterTypes.length;
		if (paras != null) {
			this.paras = paras;
			this.paraValues = new HashMap<>(paras.size());
		}
	}

	public void invoke(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		// 添加主目录属性
		request.setAttribute("basePath", WebContextUtils.getBasePath(request));
		if (this.paras != null) {
			fillRequest(request);
			logger.debug("赋值PatternAction");
		}

		ActionSupport action = null;
		try {
			action = (ActionSupport) StrutsContext.getBean(this.declaringClass);
			action.setRequest(request);
			action.setResponse(response);
			Object resultObj;
			if (this.length > 0) {
				resultObj = actionMethod.invoke(action, buildArgs(request, response));
			} else
				resultObj = actionMethod.invoke(action);
			if (resultObj != null) {
				new ResponseWrapper(request, response).doResponse(resultObj);
			}
		} catch (Exception e) {
			logger.error("Action call " + this.declaringClass.getName() + "." + actionMethod.getName() + " faild", e);
		} finally {
			if (action != null) {
				action.destory();
				logger.debug("The action has been destroyed");
			}
		}
	}

	private Object[] buildArgs(HttpServletRequest request, HttpServletResponse response) {
		Object[] objArray = new Object[this.length];
		for (int i = 0; i < this.length; i++) {
			if (this.parameterTypes[i].toString().equals("interface javax.servlet.http.HttpSession")) {
				objArray[i] = request.getSession();
			} else if (this.parameterTypes[i].toString().equals("interface javax.servlet.http.HttpServletRequest")) {
				objArray[i] = request;
			} else if (this.parameterTypes[i].toString().equals("interface javax.servlet.http.HttpServletResponse")) {
				objArray[i] = response;
			}
		}
		return objArray;
	}

	public void setParasValue(Matcher match) {
		paraValues.clear();
		int num = 1;
		for (String para : paras) {
			paraValues.put(para, match.group(num));
			num++;
		}
	}

	private void fillRequest(HttpServletRequest request) {
		Set<String> keys = paraValues.keySet();
		for (String key : keys) {
			request.setAttribute(key, paraValues.get(key));
		}
	}

}
