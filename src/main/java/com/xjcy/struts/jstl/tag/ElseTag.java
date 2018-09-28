package com.xjcy.struts.jstl.tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.xjcy.util.LoggerUtils;

public class ElseTag extends BodyTagSupport
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7339585316460624996L;
	private static final LoggerUtils logger = LoggerUtils.from(ElseTag.class);

	@Override
	public int doStartTag() throws JspException
	{
		return EVAL_BODY_BUFFERED;
	}
	
	@Override
	public int doEndTag() throws JspException
	{
		CaseTag parent = (CaseTag) this.getParent();
		if (parent.isOk() == false)
		{
			try
			{
				// 利用getString方法得到字符串
				String content = this.getBodyContent().getString();
				// 输出到浏览器
				this.pageContext.getOut().append(content.trim());
				parent.setOk(true);
			}
			catch (IOException e)
			{
				logger.error("输出if标签失败", e);
			}
		}
		return EVAL_PAGE;
	}
}
