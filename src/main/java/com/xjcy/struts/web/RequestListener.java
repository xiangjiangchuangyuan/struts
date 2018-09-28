package com.xjcy.struts.web;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;

import com.xjcy.util.LoggerUtils;

public class RequestListener implements ServletRequestListener
{
	private static final LoggerUtils logger = LoggerUtils.from(RequestListener.class);

	@Override
	public void requestDestroyed(ServletRequestEvent arg0)
	{
		HttpServletRequest request = (HttpServletRequest) arg0.getServletRequest();

		long time = System.currentTimeMillis() - (Long) request.getAttribute("dateCreated");

		if (logger.isDebugEnabled())
			logger.debug("Request destory IP=>" + getRemoteAddr(request) + " time=>" + time + "ms");
	}

	@Override
	public void requestInitialized(ServletRequestEvent arg0)
	{
		HttpServletRequest request = (HttpServletRequest) arg0.getServletRequest();

		String uri = request.getRequestURI();
		uri = request.getQueryString() == null ? uri : (uri + "?" + request.getQueryString());

		request.setAttribute("dateCreated", System.currentTimeMillis());

		if (logger.isDebugEnabled())
			logger.debug("Request init IP=>" + getRemoteAddr(request) + " url=>" + uri);
	}

	private String getRemoteAddr(HttpServletRequest request)
	{
		String ip = request.getRemoteAddr();
		if ("127.0.0.1".equals(ip))
			return request.getHeader("X-Real-IP");
		return ip;
	}

}
