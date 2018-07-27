package com.xjcy.struts;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public abstract class ActionInterceptor
{
	private static final Logger logger = Logger.getLogger(ActionInterceptor.class);
			
	public abstract boolean intercept(HttpServletRequest arg0, HttpServletResponse arg1);
	
	public abstract List<String> exceptPaths();
	
	protected void printParameters(HttpServletRequest arg0) {
		logger.debug("Content-type => " + arg0.getContentType());
		if (!(arg0.getContentType() + "").contains("/json")) {
			Set<String> paras = arg0.getParameterMap().keySet();
			for (String para : paras) {
				logger.debug("[PARA]" + para + "=>" + arg0.getParameter(para));
			}
		}
	}
}
