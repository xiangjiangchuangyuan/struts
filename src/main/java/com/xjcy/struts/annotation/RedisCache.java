package com.xjcy.struts.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;

@Retention(RUNTIME)
@Target(ElementType.METHOD)
public @interface RedisCache
{
	int value() default 60;
}

