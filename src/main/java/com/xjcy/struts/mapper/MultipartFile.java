package com.xjcy.struts.mapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.tomcat.util.http.fileupload.FileItemStream;

import com.xjcy.util.FileUtils;
import com.xjcy.util.StringUtils;

public class MultipartFile
{
	private String fileName;
	private String extension;
	private Long size;
	private InputStream inputStream;
	private String fieldName;
	private String contentType;

	public Long getSize()
	{
		return size;
	}

	public MultipartFile(FileItemStream stream) throws IOException
	{
		this.fileName = stream.getName();
		this.fieldName = stream.getFieldName();
		this.inputStream = stream.openStream();
		this.size = (long) this.inputStream.available();
		this.contentType = stream.getContentType();
		if (!StringUtils.isEmpty(fileName))
		{
			int last = fileName.lastIndexOf(".");
			if (last > 0)
				this.extension = fileName.substring(last).toLowerCase();
			else
				this.extension = "";
		}
	}
	
	/**
	 * 将流保存到文件
	 * @param dest
	 * @return
	 */
	public boolean transferTo(File dest)
	{
		if(this.inputStream == null)
			return false;
		return FileUtils.saveFile(dest, this.inputStream);
	}

	public String getFieldName()
	{
		return fieldName;
	}

	public String getContentType()
	{
		return contentType;
	}

	public InputStream getInputStream()
	{
		return inputStream;
	}

	public String getFileName()
	{
		return fileName;
	}

	public String getExtension()
	{
		return extension;
	}

	public boolean isPic()
	{
		if (StringUtils.isEmpty(extension))
			return false;
		return ".jpg".equals(extension) || ".png".equals(extension) || ".bmp".equals(extension)
				|| ".gif".equals(extension) || ".jpeg".equals(extension);
	}
	
	public boolean isVedio()
	{
		if (StringUtils.isEmpty(extension))
			return false;
		return ".avi".equals(extension) || ".mp4".equals(extension) || ".wmv".equals(extension)
				|| ".flv".equals(extension) || ".mov".equals(extension);
	}

	public void clear()
	{
		this.fileName = null;
		this.extension = null;
		this.size = null;
		this.inputStream = null;
	}
}
