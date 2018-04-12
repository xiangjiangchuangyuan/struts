package com.xjcy.struts.wrapper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

public class JSPClassLoader extends ClassLoader
{
	private static final Logger logger = Logger.getLogger(JSPClassLoader.class);

	private File classFile;

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
			int data = input.read();

			while (data != -1)
			{
				buffer.write(data);
				data = input.read();
			}
			input.close();
			byte[] classData = buffer.toByteArray();

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
		try
		{
			super.finalize();
		}
		catch (Throwable e)
		{
			logger.error("finalize faild", e);
		}
	}
}
