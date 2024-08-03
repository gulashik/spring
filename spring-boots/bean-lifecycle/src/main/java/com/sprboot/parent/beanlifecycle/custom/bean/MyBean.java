package com.sprboot.parent.beanlifecycle.custom.bean;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) // нужен RUNTIME
@Target(ElementType.TYPE)
public @interface MyBean {
}
