package com.xjcy.struts.web;

import java.util.Set;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import com.xjcy.struts.StrutsFilter;
import com.xjcy.util.LoggerUtils;

public class StrutsServletContainerInitializer implements ServletContainerInitializer
{

	private static final LoggerUtils logger = LoggerUtils.from(StrutsServletContainerInitializer.class);

	@Override
	public void onStartup(Set<Class<?>> arg0, ServletContext arg1) throws ServletException
	{
		// 添加session监听
		arg1.addListener(SessionListener.class);
		// 增加filter
		arg1.addFilter("StrutsFilter", StrutsFilter.class);
		
		if (logger.isDebugEnabled())
			logger.debug("Container startup with SessionListener, StrutsFilter");
	}
}
