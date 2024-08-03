package com.sprboot.parent.beanlifecycle.custom.bean;

import org.springframework.stereotype.Component;

@MyBean
// хотя проще поставить @Component
public class TestMyBean {
    public TestMyBean() {}
    public void printMe() {
        System.out.println("TestMyBean");
    }
}
