package com.xjcy.struts.wrapper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.xjcy.util.LoggerUtils;

public class JSPClassLoader extends ClassLoader
{
	private static final LoggerUtils logger = LoggerUtils.from(JSPClassLoader.class);

	private File classFile;
	byte[] classData;

	public JSPClassLoader(File file, ClassLoader parent)
	{
		super(parent);
		this.classFile = file;
	}

	public Class<?> loadClass(String name) throws ClassNotFoundException
	{
		if (!name.startsWith("org.apache.jsp."))
			return super.loadClass(name);

		logger.debug("Loading class " + name);
		try
		{
			InputStream input = new FileInputStream(classFile);
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			
			byte[] tmp = new byte[128];  
	        int read = 0;  
	        while ((read = input.read(tmp)) != -1) {  
	            buffer.write(tmp, 0, read);  
	        }  
			input.close();
			
			classData = buffer.toByteArray();
			buffer.close();
			return defineClass(name, classData, 0, classData.length);
		}
		catch (IOException e)
		{
			logger.error("Load class '" + name + "' faile", e);
		}
		return null;
	}

	public void close()
	{
		classFile = null;
		classData = null;
	}
}
