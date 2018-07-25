package com.xjcy.struts.mapper;

import java.lang.reflect.Field;

public class SpringBean
{
	private Field field;
	private Class<?> target;

	public SpringBean(Field field) {
		this.field = field;
	}

	public boolean isTarget(Class<?> cla) {
		return field.getType().isAssignableFrom(cla);
	}

	public void setTargetClass(Class<?> cla) {
		this.target = cla;
	}

	public Class<?> getTargetClass() {
		return this.target;
	}

	public void setTargetValue(Object obj, Object bean) {
		try {
			field.setAccessible(true);
			field.set(obj, bean);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		} finally {
			field.setAccessible(false);
		}
	}

}
