package com.xjcy.struts.wrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.xjcy.struts.context.WebContextUtils;
import com.xjcy.struts.mapper.JSONMap;
import com.xjcy.struts.mapper.ModelAndView;
import com.xjcy.util.ObjectUtils;
import com.xjcy.util.STR;
import com.xjcy.util.StringUtils;

public class ResponseWrapper {
	private static final Logger logger = Logger.getLogger(ResponseWrapper.class);

	private static final int minCompress = 256; // 最小压缩值
	private static final int maxPrintLog = 4096; // 最大日志打印

	private final JSPWrapper jspWrapper;
	private final HttpServletRequest request;
	private final HttpServletResponse response;

	public ResponseWrapper(HttpServletRequest request, HttpServletResponse response, JSPWrapper jspWrapper) {
		this.request = request;
		this.response = response;
		this.jspWrapper = jspWrapper;
	}

	public void doResponse(Object result) throws ServletException, IOException {
		if (result instanceof ModelAndView)
			dealView((ModelAndView) result);
		else if (result instanceof String)
			dealString(result.toString());
		else if (result instanceof JSONMap)
			dealJSON(result.toString());
		else
			throw new ServletException("不支持的返回类型 " + result.getClass());
	}

	private void dealView(ModelAndView mav) throws ServletException, IOException {
		String view = mav.getViewName();
		if (view.endsWith(".html") || view.endsWith(".htm"))
			request.getRequestDispatcher(view).forward(request, response);
		else {
			mav.fillRequest(request);
			jspWrapper.processJsp(view, request, response);
		}
		if (logger.isDebugEnabled())
			logger.debug("Forward to " + view);
	}

	private void dealString(String text) throws IOException, ServletException {
		response.setContentType(STR.CONTENT_TYPE_TEXT);
		writeResponse(response, request, text);
		if (logger.isDebugEnabled())
			logger.debug("[TEXT]" + text);
	}

	private void dealJSON(String json) throws IOException, ServletException {
		String jsonpCallback = request.getParameter("callback");
		boolean isEmpty = StringUtils.isEmpty(jsonpCallback);
		if (isEmpty)
			response.setContentType(STR.CONTENT_TYPE_JSON);
		else {
			json = jsonpCallback + "(" + json + ")";
			response.setContentType(STR.CONTENT_TYPE_TEXT);
		}
		writeResponse(response, request, json);
		if (logger.isDebugEnabled()) {
			if (json.length() > maxPrintLog)
				json = json.substring(0, maxPrintLog) + "...";
			if (isEmpty)
				logger.debug("[JSON]" + json);
			else
				logger.debug("[JSONP]" + json);
		}
	}

	public static void writeResponse(HttpServletResponse response, HttpServletRequest request, String text)
			throws IOException, ServletException {
		int len = text.length();
		// 清缓存
		response.setHeader(STR.HEADER_PRAGMA, "no-cache");
		response.setHeader(STR.HEADER_CACHE_CONTROL, "no-cache");
		response.setHeader(STR.HEADER_EXPIRES, "no-cache");
		if (WebContextUtils.isGZipEncoding(request) && len > minCompress) {
			byte[] data = ObjectUtils.string2Byte(text, STR.ENCODING_UTF8);
			if (data == null)
				throw new ServletException("Response data can not be null");
			writeGZipData(response, data, len);
		} else {
			PrintWriter out = response.getWriter();
			out.print(text);
			out.close();
		}
	}

	private static void writeGZipData(HttpServletResponse response, byte[] data, int jsonLen) throws IOException {
		long start = System.nanoTime();
		ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
		GZIPOutputStream output = new GZIPOutputStream(byteOutput);
		output.write(data);
		output.close();
		byte[] gzipData = byteOutput.toByteArray();
		byteOutput.close();
		if (logger.isDebugEnabled())
			logger.debug("Compress gzip from " + jsonLen + " to " + gzipData.length + " in "
					+ (System.nanoTime() - start) + " ns");
		response.addHeader(STR.HEADER_CONTENT_ENCODING, STR.ENCODING_GZIP);
		response.setContentLength(gzipData.length);
		ServletOutputStream output2 = response.getOutputStream();
		output2.write(gzipData);
		output2.flush();
		output2.close();
	}
}
