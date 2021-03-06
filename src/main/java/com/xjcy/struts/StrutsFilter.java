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

import com.xjcy.struts.context.StrutsContext;
import com.xjcy.struts.context.WebContextUtils;
import com.xjcy.struts.mapper.ActionMapper;
import com.xjcy.struts.wrapper.JSPWrapper;
import com.xjcy.util.LoggerUtils;
import com.xjcy.util.STR;

public class StrutsFilter implements Filter {
	private static final LoggerUtils logger = LoggerUtils.from(StrutsFilter.class);

	private StrutsContext context;
	private JSPWrapper jspWrapper;

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		jspWrapper = new JSPWrapper(arg0.getServletContext(), WebContextUtils.isLinuxOS());
		context = new StrutsContext(arg0.getServletContext(), jspWrapper);
		if (logger.isDebugEnabled()) {
			logger.debug("Find actions " + context.actionSize() + " beans " + context.beanSize() + " interceptors "
					+ context.interceptorSize());
		}
	}

	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2)
			throws IOException, ServletException {
		// 设置编码
		arg0.setCharacterEncoding(STR.ENCODING_UTF8);
		arg1.setCharacterEncoding(STR.ENCODING_UTF8);

		HttpServletRequest request = (HttpServletRequest) arg0;
		String servletPath = request.getServletPath();
		ActionMapper action = this.context.getAction(servletPath);
		if (action == null) {
			logger.error("The action of '" + servletPath + "' not found");
			arg2.doFilter(arg0, arg1);
		} else {
			logger.debug("Find the action => " + servletPath);
			HttpServletResponse response = (HttpServletResponse) arg1;
			if (!action.checkMethod(request.getMethod())) {
				response.sendError(405, "Request method '" + request.getMethod() + "' not supported");
				logger.debug("Method not allowed");
				return;
			}
			if (!context.checkInterceptors(servletPath, request, response)) {
				logger.debug("Action is blocked by Interceptor");
				return;
			}
			action.invoke(request, response, jspWrapper);
		}
	}

	@Override
	public void destroy() {
		if (context != null) {
			context.destory();
			context = null;
		}
		if (jspWrapper != null) {
			jspWrapper.destroy();
			jspWrapper = null;
		}
		if (logger.isDebugEnabled())
			logger.debug("destroy context");
	}

}
