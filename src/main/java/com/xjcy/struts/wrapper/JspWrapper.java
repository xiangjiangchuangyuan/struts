package com.xjcy.struts.wrapper;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jasper.runtime.HttpJspBase;
import org.apache.log4j.Logger;

import com.xjcy.struts.context.JspC;
import com.xjcy.struts.context.WebContextUtils;
import com.xjcy.struts.mapper.JspCache;

/***
 * JSP响应处理
 * @author YYDF
 * 2018-04-11
 */
public class JspWrapper {
	private static final Logger logger = Logger.getLogger(JspWrapper.class);

	private final JspC jspc;

	public JspWrapper(ServletContext sc) {
		jspc = new JspC(sc, false);
	}

	public void processJsp(String jspUri, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if (WebContextUtils.isLinuxOS()) {
			if ("1".equals(request.getParameter("reload"))) {
				logger.debug("Recompile jsp " + jspUri);
				jspc.execute(jspUri);
			}
			// 线上环境获取预编译的jspServlet
			HttpJspBase jspBase = JspCache.getServlet(jspUri);
			if (jspBase == null)
				throw new IOException("Not found jsp servlet " + jspUri);
			jspBase.init(new JspServletConfig(request.getServletContext()));
			jspBase.service(request, response);
			jspBase.destroy();
		} else {
			// 直接跳转
			request.getRequestDispatcher(jspUri).forward(request, response);
		}
	}

	public class JspServletConfig implements ServletConfig {
		private ServletContext context;

		public JspServletConfig(ServletContext context) {
			this.context = context;
		}

		@Override
		public String getInitParameter(String arg0) {
			return null;
		}

		@Override
		public Enumeration<String> getInitParameterNames() {
			return null;
		}

		@Override
		public ServletContext getServletContext() {
			return this.context;
		}

		@Override
		public String getServletName() {
			return "struts_jsp";
		}
	}
}
