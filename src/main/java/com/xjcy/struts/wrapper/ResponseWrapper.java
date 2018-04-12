package com.xjcy.struts.wrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.xjcy.struts.context.WebContextUtils;
import com.xjcy.struts.mapper.JSONMap;
import com.xjcy.struts.mapper.ModelAndView;
import com.xjcy.util.ObjectUtils;
import com.xjcy.util.RedisUtils;
import com.xjcy.util.STR;

public class ResponseWrapper
{
	private static final Logger logger = Logger.getLogger(ResponseWrapper.class);

	private final JSPWrapper jspWrapper;
	private Class<?> returnType;
	private Object resultObj;
	private boolean redisCache = false;
	private int cacheSeconds = 0;
	
	public ResponseWrapper(ServletContext sc)
	{
		jspWrapper = new JSPWrapper(sc);
	}

	public void setReturnObj(Class<?> returnType, Object resultObj)
	{
		this.returnType = returnType;
		this.resultObj = resultObj;
	}
	
	public void setCache(boolean redisCache, int cacheSeconds)
	{
		this.redisCache = redisCache;
		this.cacheSeconds = cacheSeconds;
	}

	public void doResponse(HttpServletRequest request, HttpServletResponse response, String json)
			throws IOException, ServletException
	{
		dealJSON(json, request, response);
	}

	public void doResponse(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		if (returnType.equals(Void.class) || returnType.equals(void.class))
			dealNone(resultObj, request, response);
		else if (returnType.equals(ModelAndView.class))
			dealView(resultObj, request, response);
		else if (returnType.equals(String.class))
			dealString(resultObj, request, response);
		else if (returnType.equals(JSONMap.class))
			dealJSON((JSONMap) resultObj, request, response);
		else 
			throw new ServletException("不支持的返回类型 " + returnType.getName());
	}

	private void dealNone(Object resultObj, HttpServletRequest request, HttpServletResponse response)
	{
		if (logger.isDebugEnabled())
			logger.debug("自定义Response");
	}

	private void dealView(Object resultObj, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		ModelAndView mav = (ModelAndView) resultObj;
		mav.fillRequest(request);
		jspWrapper.processJsp(mav.getViewName(), request, response);
		if (logger.isDebugEnabled())
			logger.debug("Forward to " + mav.getViewName());
	}

	private void dealString(Object resultObj, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException
	{
		String text = resultObj.toString();
		response.setContentType(STR.CONTENT_TYPE_TEXT);
		// 清缓存
		response.setHeader(STR.HEADER_PRAGMA, "No-cache");
		response.setHeader(STR.HEADER_CACHE_CONTROL, "no-cache");
		response.setDateHeader("Expires", 0);
		writeResponse(response, request, text);
		if (logger.isDebugEnabled())
			logger.debug("[TEXT]" + text);
	}

	private void dealJSON(String json, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException
	{
		response.setContentType(STR.CONTENT_TYPE_JSON);
		response.addHeader("Access-Control-Allow-Origin", "*");
		// 清缓存
		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);
		writeResponse(response, request, json);
		if (logger.isDebugEnabled())
			logger.debug("[CACHE_JSON]" + json);
	}

	private void dealJSON(JSONMap obj, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException
	{
		String json = obj.toString();
		if (this.redisCache)
		{
			String key = request.getServletPath() + "_" + request.getQueryString();
			RedisUtils.set(key, json, cacheSeconds);
		}
		response.setContentType(STR.CONTENT_TYPE_JSON);
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Method", "GET,POST,PUT");
		// 清缓存
		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);
		writeResponse(response, request, json);
		if (logger.isDebugEnabled())
			logger.debug("[JSON]" + json);
	}

	public static void writeResponse(HttpServletResponse response, HttpServletRequest request, String text)
			throws IOException, ServletException
	{
		int len = text.length();
		if (WebContextUtils.isGZipEncoding(request) && len > 256)
		{
			byte[] data = ObjectUtils.string2Byte(text, STR.ENCODING_UTF8);
			if (data == null)
				throw new ServletException("Response data can not be null");
			writeGZipData(response, data, len);
		}
		else
		{
			PrintWriter out = response.getWriter();
			out.print(text);
			out.close();
		}
	}

	private static void writeGZipData(HttpServletResponse response, byte[] data, int jsonLen) throws IOException
	{
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
		response.addHeader("Content-Encoding", "gzip");
		response.setContentLength(gzipData.length);
		ServletOutputStream output2 = response.getOutputStream();
		output2.write(gzipData);
		output2.flush();
		output2.close();
	}
}
