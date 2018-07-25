package com.xjcy.struts.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class SessionListener implements HttpSessionListener
{
	private static final Map<String, HttpSession> sessionList = new HashMap<>();
	private static final Object obj = new Object();

	@Override
	public void sessionCreated(HttpSessionEvent arg0)
	{
		HttpSession session = arg0.getSession();
		synchronized (obj)
		{
			sessionList.put(session.getId(), session);
		}
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent arg0)
	{
		HttpSession session = arg0.getSession();
		synchronized (obj)
		{
			sessionList.remove(session.getId());
		}
	}
	
	/**
	 * 获取正在使用的SESSION列表
	 * @return
	 */
	public static Map<String, HttpSession> getSessions()
	{
		return sessionList;
	}

	public static HttpSession getSession(String id) {
		return sessionList.get(id);
	}

}
