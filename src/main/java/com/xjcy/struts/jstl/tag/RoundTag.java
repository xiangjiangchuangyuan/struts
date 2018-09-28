package com.xjcy.struts.jstl.tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.xjcy.util.LoggerUtils;

public class RoundTag extends BodyTagSupport
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final LoggerUtils logger = LoggerUtils.from(RoundTag.class);

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
		try {
			try {
				if (digits > 0) {
					double d = Double.parseDouble(value);
					// 输出到浏览器
					this.pageContext.getOut().append(String.format("%." + digits + "f", d));
				} else {
					// 输出到浏览器
					this.pageContext.getOut().append(value);
				}
			} catch (Exception e) {
				logger.error("输出round标签失败", e);
				this.pageContext.getOut().append(value);
			}
		} catch (IOException e) {
			logger.error("输出round标签失败", e);
		}
		return EVAL_PAGE;
	}
	
	public static void main(String[] args) {
		System.out.println(new java.text.DecimalFormat("#.00").format(0.00D).toString());
	}
}
