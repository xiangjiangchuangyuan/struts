package com.xjcy.struts.jstl.tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.log4j.Logger;

public class RoundTag extends BodyTagSupport
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(RoundTag.class);

	private String value;

	public void setValue(String value)
	{
		this.value = value;
	}

	private int digits = 2;

	public void setDigits(int digits)
	{
		this.digits = digits;
	}

	@Override
	public int doStartTag() throws JspException
	{
		return EVAL_BODY_BUFFERED;
	}

	@Override
	public int doEndTag() throws JspException
	{
		String format = "#";
		if (digits > 0)
		{
			format += ".";
			for (int i = 0; i < digits; i++)
			{
				format += "0";
			}
		}
		try
		{
			try
			{
				double d = Double.parseDouble(value);
				// 输出到浏览器
				this.pageContext.getOut().append(new java.text.DecimalFormat(format).format(d));
			}
			catch (Exception e)
			{
				logger.error("输出round标签失败", e);
				this.pageContext.getOut().append(format.replace("#", "0"));
			}
		}
		catch (IOException e)
		{
			logger.error("输出round标签失败", e);
		}
		return EVAL_PAGE;
	}
	
	public static void main(String[] args) {
		System.out.println(new java.text.DecimalFormat("#.00").format(0.00D).toString());
	}
}
