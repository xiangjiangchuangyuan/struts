package com.xjcy.struts.context;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.jsp.tagext.TagLibraryInfo;

import org.apache.jasper.JasperException;
import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.Options;
import org.apache.jasper.compiler.JspConfig;
import org.apache.jasper.compiler.JspRuntimeContext;
import org.apache.jasper.compiler.TagPluginManager;
import org.apache.jasper.compiler.TldCache;
import org.apache.jasper.servlet.JspCServletContext;
import org.apache.jasper.servlet.TldScanner;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

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
	private final List<String> pages = new Vector<>();

	private static final String Encoding = "UTF-8";

	public JspC(ServletContext sc, boolean clear) {
		this(sc, null, clear);
	}

	public JspC(ServletContext sc, List<String> jspList, boolean clear) {
		this.uriRoot = sc.getRealPath("/");
		String outputDir = sc.getRealPath(StrutsContext.CLASS_PATH);
		this.scratchDir = new File(outputDir);
		try {
			if (jspList != null) {
				pages.addAll(jspList);
				logger.debug("Add pages with list " + jspList.size());
			}
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

	public void setJspFiles(String jspFiles) {
		if (jspFiles == null) {
			return;
		}
		StringTokenizer tok = new StringTokenizer(jspFiles, ",");
		while (tok.hasMoreTokens()) {
			pages.add(tok.nextToken());
		}
	}

	public void execute() {
		long start = System.currentTimeMillis();
		Iterator<String> iter = pages.iterator();
		while (iter.hasNext()) {
			processFile(iter.next());
		}
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

	protected void processFile(String file) {
		try {
			String jspUri = file.replace('\\', '/');
			JspCompilationContext clctxt = new JspCompilationContext(jspUri, this, context, null, rctxt);

			logger.debug("Compiling file: " + file);
			clctxt.createCompiler().compile(true, true);
		} catch (Exception e) {
			logger.error("Compile '" + file + "' faild", e);
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
