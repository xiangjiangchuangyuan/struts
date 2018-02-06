package com.xjcy.struts.wrapper;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.tomcat.util.http.fileupload.FileItemFactory;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;

/**
 * 解析上传文件类
 * 
 * @author 张梦龙 2016-09-23
 */
public class MultipartRequestWrapper
{
	private boolean isMultipartRequest = false;

	/**
	 * 解析上传的文件
	 * 
	 * @param request
	 *            请求
	 */
	public MultipartRequestWrapper(HttpServletRequest request)
	{
		this.isMultipartRequest = ServletFileUpload.isMultipartContent(request);
	}

	/**
	 * 获取上传的文件
	 * 
	 * @param request
	 * @return
	 * @throws FileUploadException
	 * @throws IOException
	 */
	public FileItemIterator processRequest(HttpServletRequest request) throws FileUploadException, IOException
	{
		FileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		return upload.getItemIterator(request);
	}
	
//	/**
//	 * 获取上传的文件
//	 * 
//	 * @param request
//	 * @return
//	 * @throws FileUploadException
//	 * @throws IOException
//	 */
//	public Map<String, List<FileItem>> processRequest2(HttpServletRequest request) throws FileUploadException, IOException
//	{
//		FileItemFactory factory = new DiskFileItemFactory();
//		ServletFileUpload upload = new ServletFileUpload(factory);
//		return upload.parseParameterMap(request);
//	}

	public boolean isMultipartRequest()
	{
		return this.isMultipartRequest;
	}
}
