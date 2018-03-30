package com.xjcy.struts;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.util.Streams;

import com.xjcy.struts.mapper.MultipartFile;
import com.xjcy.struts.web.SessionListener;
import com.xjcy.struts.wrapper.MultipartRequestWrapper;
import com.xjcy.util.DateEx;
import com.xjcy.util.StringUtils;

public abstract class ActionSupport {
	private static final Logger logger = Logger.getLogger(ActionSupport.class);

	private HttpServletRequest httpServletRequest;
	private HttpServletResponse httpServletResponse;
	private boolean isMultipartRequest = false;
	private final Map<String, String> paras = new HashMap<>();
	private final Map<String, MultipartFile> multipartFiles = new HashMap<>();

	private static final String VAL_UNDEFINED = "undefined";
	private static final String VAL_NULL = "null";

	protected HttpServletRequest getRequest() {
		return httpServletRequest;
	}

	protected HttpServletResponse getResponse() {
		return httpServletResponse;
	}

	protected HttpSession getSession() {
		return httpServletRequest.getSession();
	}

	protected Map<String, HttpSession> getSessions() {
		return SessionListener.getSessions();
	}

	protected String getParameter(String arg0) {
		String str = null;
		if (isMultipartRequest)
			str = paras.get(arg0);
		else {
			str = httpServletRequest.getParameter(arg0);
			if (StringUtils.isEmpty(str)) {
				Object obj = httpServletRequest.getAttribute(arg0);
				str = (obj != null) ? obj.toString() : null;
			}
		}
		if (VAL_UNDEFINED.equals(str))
			return null;
		return str;
	}

	protected MultipartFile getMultipartFile(String arg0) {
		return multipartFiles.get(arg0);
	}

	protected <T> T getPostData(Class<T> cla) {
		return getPostData(cla, VAL_NULL);
	}

	protected <T> T getPostData(Class<T> cla, String ignore) {
		Field[] fields = cla.getDeclaredFields();
		if (fields.length > 0) {
			try {
				T tt = cla.newInstance();
				String str;
				String fieldType;
				for (Field field : fields) {
					if (!VAL_NULL.equals(ignore) && field.getName().equals(ignore))
						continue;
					str = getParameter(field.getName());
					fieldType = field.getGenericType().toString();
					if (!StringUtils.isEmpty(str)) {
						field.setAccessible(true);
						if ("class java.lang.Integer".equals(fieldType))
							field.set(tt, Integer.valueOf(str));
						else if ("class java.util.Date".equals(fieldType))
							field.set(tt, DateEx.toDate(str));
						else if ("class java.lang.Double".equals(fieldType))
							field.set(tt, Double.parseDouble(str));
						else if ("class java.lang.Long".equals(fieldType))
							field.set(tt, Long.parseLong(str));
						else if ("class java.lang.Boolean".equals(fieldType))
							field.set(tt, Boolean.parseBoolean(str));
						else
							field.set(tt, str);
						field.setAccessible(false);
						if (logger.isDebugEnabled())
							logger.debug("getPostData => " + field.getName() + "[" + str + "]");
					}
				}
				return tt;
			} catch (Exception e) {
				logger.debug("获取页面数据失败", e);
			}
		}
		return null;
	}

	public void setRequest(HttpServletRequest request) {
		if (request != null) {
			// 判断是否为文件Action
			MultipartRequestWrapper wrapper = new MultipartRequestWrapper(request);
			if (wrapper.isMultipartRequest()) {
				this.isMultipartRequest = true;
				if (logger.isDebugEnabled())
					logger.debug("Request is multipart");
				try {
					FileItemIterator files = wrapper.processRequest(request);
					if (files != null) {
						while (files.hasNext()) {
							FileItemStream stream = files.next();
							if (stream.isFormField())
								paras.put(stream.getFieldName(), Streams.asString(stream.openStream(), "utf-8"));
							else {
								MultipartFile file = new MultipartFile(stream);
								paras.put(file.getFieldName(), processMultipartFile(file));
								multipartFiles.put(file.getFieldName(), file);
							}
						}
					}
				} catch (FileUploadException | IOException e) {
					logger.error("Parsing upload file failed", e);
				}
			}
		}
		this.httpServletRequest = request;
	}

	protected abstract String processMultipartFile(MultipartFile file) throws IOException;

	public void setResponse(HttpServletResponse response) {
		this.httpServletResponse = response;
	}
}
