package com.xjcy.struts.jstl.tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.xjcy.util.LoggerUtils;

public class CutStringTag extends BodyTagSupport {
	/**
	 * 
	 */
	private static final long serialVersionUID = -327645925586914122L;
	private static final LoggerUtils logger = LoggerUtils.from(CutStringTag.class);

	private String str;
	private int len;

	public void setStr(String str) {
		this.str = str;
	}

	public void setLen(int len) {
		this.len = len;
	}

	@Override
	public int doStartTag() throws JspException {
		return EVAL_BODY_BUFFERED;
	}

	@Override
	public int doEndTag() throws JspException {
		if (str != null && str.length() > len) {
			str = str.substring(0, len);
		}
		// 输出到浏览器
		try {
			this.pageContext.getOut().append(str);
		} catch (IOException e) {
			logger.error("输出substr标签失败", e);
		}
		return EVAL_PAGE;
	}
}
