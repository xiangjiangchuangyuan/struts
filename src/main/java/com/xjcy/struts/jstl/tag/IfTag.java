package com.xjcy.struts.jstl.tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.xjcy.util.LoggerUtils;

public class IfTag extends BodyTagSupport
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -327645925586914122L;
	private static final LoggerUtils logger = LoggerUtils.from(IfTag.class);

	private String test;

	public void setTest(String test)
	{
		this.test = test;
	}

	@Override
	public int doStartTag() throws JspException
	{
		return EVAL_BODY_BUFFERED;
	}

	@Override
	public int doEndTag() throws JspException
	{
		// 利用getString方法得到字符串
		String content = this.getBodyContent().getString();
		if(Boolean.parseBoolean(test))
		{
			try
			{
				// 输出到浏览器
				this.pageContext.getOut().append(content.trim());
			}
			catch (IOException e)
			{
				logger.error("输出if标签失败", e);
			}
		}
		return EVAL_PAGE;
	}
}
