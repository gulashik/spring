package com.sprboot.parent.beanlifecycle.custom.bean;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
// можем дополнить, что включать/исключать
@ComponentScan(includeFilters = {@ComponentScan.Filter(type = FilterType.ANNOTATION, classes = MyBean.class)})
public class ConfigForMyBean {
}
