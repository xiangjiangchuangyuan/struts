package com.xjcy.struts.context;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.jsp.tagext.TagLibraryInfo;

import org.apache.jasper.JasperException;
import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.Options;
import org.apache.jasper.compiler.JspConfig;
import org.apache.jasper.compiler.JspRuntimeContext;
import org.apache.jasper.compiler.JspUtil;
import org.apache.jasper.compiler.TagPluginManager;
import org.apache.jasper.compiler.TldCache;
import org.apache.jasper.runtime.HttpJspBase;
import org.apache.jasper.servlet.JspCServletContext;
import org.apache.jasper.servlet.TldScanner;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.xjcy.struts.mapper.JspCache;
import com.xjcy.struts.wrapper.JspClassLoader;
import com.xjcy.util.FileUtils;

/***
 * 预编译jsp文件，移除ant引用 2018-04-10
 * 
 * @author YYDF
 *
 */
public class JspC implements Options {

	static final Logger logger = Logger.getLogger(JspC.class);

	private String uriRoot;
	private File scratchDir;
	private JspCServletContext context;
	private TldScanner scanner;
	private TldCache tldCache;
	private JspRuntimeContext rctxt;
	private JspConfig jspConfig;
	private TagPluginManager tagPluginManager;

	private static final String Encoding = "UTF-8";


	public JspC(ServletContext sc, boolean clear) {
		this.uriRoot = sc.getRealPath("/");
		String outputDir = sc.getRealPath(StrutsContext.CLASS_PATH);
		this.scratchDir = new File(outputDir);
		try {
			initServletContext(this.getClass().getClassLoader());
			if (clear) {
				File output = new File(outputDir + "/org");
				if (output.exists()) {
					FileUtils.deleteDir(output);
					logger.debug("Deleted dir " + output.getPath());
				}
			}
		} catch (JasperException | IOException e) {
			logger.error("Init context faild", e);
		}
	}

	public void execute(List<String> jspList) {
		long start = System.currentTimeMillis();
		for (String jsp : jspList) {
			processFile(jsp);
		}
		logger.debug("Jsp servlet build success in " + (System.currentTimeMillis() - start) + " ms");
	}
	
	public void execute(String jspUri) {
		long start = System.currentTimeMillis();
		processFile(jspUri);
		logger.debug("Jsp servlet build success in " + (System.currentTimeMillis() - start) + " ms");
	}

	protected void initServletContext(ClassLoader loader) throws IOException, JasperException {
		URL resourceBase = new File(uriRoot).getCanonicalFile().toURI().toURL();
		context = new JspCServletContext(new PrintWriter(System.out), resourceBase, loader, false, false);

		try {
			scanner = new TldScanner(context, false, false, false);
			scanner.scan();
		} catch (SAXException e) {
			throw new JasperException(e);
		}
		tldCache = new TldCache(context, scanner.getUriTldResourcePathMap(), scanner.getTldResourcePathTaglibXmlMap());
		rctxt = new JspRuntimeContext(context, this);
		jspConfig = new JspConfig(context);
		tagPluginManager = new TagPluginManager(context);
	}

	protected void processFile(String jspUri) {
		try {
			JspCompilationContext clctxt = new JspCompilationContext(jspUri, this, context, null, rctxt);

			logger.debug("Compiling and cache file: " + jspUri);
			clctxt.createCompiler().compile(true, true);
			
			JspCache.put(jspUri, getServlet(scratchDir, jspUri));
		} catch (Exception e) {
			logger.error("Compile '" + jspUri + "' faild", e);
		}
	}

	private static HttpJspBase getServlet(File output, String jspUri) {
		try {
			String className = JspUtil.makeJavaPackage(jspUri);
			File servletFile = WebContextUtils.getJspServletFile(output, className.replace(".", "/") + ".class");
			JspClassLoader loader = new JspClassLoader(servletFile, Thread.currentThread().getContextClassLoader());
			Class<?> cla = loader.loadClass("org.apache.jsp." + className);
			loader.close();
			return (HttpJspBase) cla.newInstance();
		} catch (IllegalAccessException | ClassNotFoundException | InstantiationException e) {
			logger.error("获取jspServlet失败", e);
			return null;
		}
	}
	
	@Override
	public boolean getErrorOnUseBeanInvalidClassAttribute() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getKeepGenerated() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPoolingEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getMappedFile() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getClassDebugInfo() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getCheckInterval() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean getDevelopment() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getDisplaySourceFragment() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSmapSuppressed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSmapDumped() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getTrimSpaces() {
		return true;
	}

	@Override
	public String getIeClassId() {
		return "clsid:8AD9C840-044E-11D1-B3E9-00805F499D93";
	}

	@Override
	public File getScratchDir() {
		return scratchDir;
	}

	@Override
	public String getClassPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCompiler() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCompilerTargetVM() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCompilerSourceVM() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCompilerClassName() {
		return "org.apache.jasper.compiler.JDTCompiler";
	}

	@Override
	public TldCache getTldCache() {
		return tldCache;
	}

	@Override
	public String getJavaEncoding() {
		return Encoding;
	}

	@Override
	public boolean getFork() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public JspConfig getJspConfig() {
		return jspConfig;
	}

	@Override
	public boolean isXpoweredBy() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public TagPluginManager getTagPluginManager() {
		return tagPluginManager;
	}

	@Override
	public boolean genStringAsCharArray() {
		return true;
	}

	@Override
	public int getModificationTestInterval() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean getRecompileOnFail() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCaching() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Map<String, TagLibraryInfo> getCache() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getMaxLoadedJsps() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getJspIdleTimeout() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean getStrictQuoteEscaping() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getQuoteAttributeEL() {
		// TODO Auto-generated method stub
		return false;
	}
}
