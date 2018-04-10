package com.xjcy.struts.wrapper;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jasper.compiler.JspUtil;
import org.apache.jasper.runtime.HttpJspBase;
import org.apache.log4j.Logger;

import com.xjcy.struts.context.JspC;
import com.xjcy.struts.context.WebContextUtils;

public class JspWrapper
{
	private static final Logger logger = Logger.getLogger(JspWrapper.class);

	private static final Map<String, HttpJspBase> jspServlets = new HashMap<>();
	private static final Map<String, Long> jspLastTimes = new HashMap<>();
	private final JspC jspc;
	
	public JspWrapper(ServletContext sc){
		jspc = new JspC(sc, false);
	}

	public void processJsp(String jspUri, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		// 线上环境执行预编译的jspServlet
		if (WebContextUtils.isLinuxOS())
		{
			ServletContext context = request.getServletContext();
			File jspFile = new File(context.getRealPath(jspUri));
			if (!jspFile.exists())
				throw new IOException("Not found the jsp file " + jspUri);
			Long lastModified = jspLastTimes.get(jspUri);
			HttpJspBase jspBase;
			if (jspServlets.containsKey(jspUri) && lastModified == jspFile.lastModified())
				jspBase = jspServlets.get(jspUri);
			else
			{
				// 判断已经加载过，重新编译jsp文件
				if (lastModified != null && del(jspUri, context))
				{
					if (reCompiler(jspUri, context))
						jspBase = getServlet(context, jspUri);
					else
						throw new IOException("Rebuild jsp servlet " + jspUri + " faild");
				}
				else
					jspBase = getServlet(context, jspUri);
				if (jspBase == null)
					throw new IOException("Not found jsp servlet " + jspUri);
				jspServlets.put(jspUri, jspBase);
			}
			jspBase.init(new JspServletConfig(context));
			jspBase.service(request, response);
			jspBase.destroy();
		}
		else
		{
			// 直接跳转
			request.getRequestDispatcher(jspUri).forward(request, response);
		}
	}

	private boolean del(String jspUri, ServletContext context)
	{
		String className = JspUtil.makeJavaPackage(jspUri);
		File file1 = WebContextUtils.getJspServletFile(context, className.replace(".", "/") + ".class");
		return file1.delete();
	}

	private boolean reCompiler(String jspUri, ServletContext context)
	{
		jspc.setJspFiles(jspUri.substring(1, jspUri.length()));
		jspc.execute();
		return true;
	}

	private static HttpJspBase getServlet(ServletContext context, String jspUri) throws IOException
	{
		try
		{
			String className = JspUtil.makeJavaPackage(jspUri);
			File servletFile = WebContextUtils.getJspServletFile(context, className.replace(".", "/") + ".class");
			JspClassLoader loader = new JspClassLoader(servletFile, Thread.currentThread().getContextClassLoader());
			Class<?> cla = loader.loadClass("org.apache.jsp." + className);
			loader.close();
			// 缓存编译的jsp文件最后修改时间
			jspLastTimes.put(jspUri, servletFile.lastModified());
			return (HttpJspBase) cla.newInstance();
		}
		catch (IllegalAccessException | ClassNotFoundException | InstantiationException e)
		{
			logger.error("获取jspServlet失败", e);
			return null;
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
