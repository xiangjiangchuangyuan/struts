package com.xjcy.struts;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.xjcy.struts.context.StrutsContext;
import com.xjcy.struts.context.WebContextUtils;
import com.xjcy.struts.mapper.ActionMapper;
import com.xjcy.struts.wrapper.ResponseWrapper;
import com.xjcy.util.RedisUtils;
import com.xjcy.util.STR;
import com.xjcy.util.StringUtils;

public class StrutsFilter implements Filter
{
	private static final Logger logger = Logger.getLogger(StrutsFilter.class);
	private static String basePath;
	private static String serverName;
	private StrutsContext context;
	private ResponseWrapper responseWrapper;

	@Override
	public void destroy()
	{
		context.clear();
		responseWrapper = null;
		basePath = null;
		serverName = null;
		context = null;
		if (logger.isDebugEnabled())
			logger.debug("destroy context");
	}

	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2)
			throws IOException, ServletException
	{
		// 设置编码
		arg0.setCharacterEncoding(STR.ENCODING_UTF8);
		arg1.setCharacterEncoding(STR.ENCODING_UTF8);
		HttpServletRequest request = (HttpServletRequest) arg0;
		String servletPath = request.getServletPath();
		if (logger.isDebugEnabled())
			logger.debug("Filter => " + servletPath);
		ActionMapper action = this.context.getAction(servletPath);
		if (action == null)
		{
			logger.error("The action of '" + servletPath + "' not found");
			arg2.doFilter(arg0, arg1);
		}
		else
		{
			HttpServletResponse response = (HttpServletResponse) arg1;
			String key = servletPath + "_" + request.getQueryString();
			if (action.getRedisCache() && RedisUtils.exists(key))
			{
				String json = RedisUtils.get(key);
				responseWrapper.doResponse(request, response, json);
				return;
			}
			// 添加主目录属性
			request.setAttribute("basePath", getBasePath(request));
			if (action.isPatternAction())
			{
				action.fillRequest(request);
				logger.debug("赋值PatternAction：" + servletPath);
			}

			try
			{
				if (!context.checkInterceptors(request, response))
				{
					if (logger.isDebugEnabled())
						logger.debug("Action is blocked by Interceptor");
					return;
				}
				logger.debug("Find the action => " + action.toString());
				ActionSupport as = (ActionSupport) context.getBean(action.getController());
				Object resultObj = action.invoke(as, request, response);
				if (resultObj != null)
				{
					responseWrapper.setReturnObj(action.getReturnType(), resultObj);
					responseWrapper.setCache(action.getRedisCache(), action.getCacheSeconds());
					responseWrapper.doResponse(request, response);
				}
			}
			catch (IllegalArgumentException e)
			{
				logger.error("执行 action '" + action.getController().getName() + "' 失败", e);
			}
		}
	}

	private static Object getBasePath(HttpServletRequest request)
	{
		String server = request.getServerName();
		if (StringUtils.isEmpty(basePath) || !server.equals(serverName))
		{
			serverName = server;
			String path = "/ROOT".equals(request.getContextPath()) ? "" : request.getContextPath();
			String port = (request.getServerPort() == 80 ? "" : ":" + request.getServerPort());
			basePath = request.getScheme() + "://" + server + port + path + "/";
		}
		return basePath;
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException
	{
		context = WebContextUtils.getWebApplicationContext(arg0.getServletContext());
		if (logger.isDebugEnabled())
			logger.debug("Find actions " + context.actionSize() + " beans " + context.beanSize() + " interceptors "
					+ context.interceptorSize());
		responseWrapper = new ResponseWrapper(arg0.getServletContext());
	}

}
