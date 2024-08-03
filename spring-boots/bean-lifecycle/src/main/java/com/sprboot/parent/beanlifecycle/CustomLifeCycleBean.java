package com.sprboot.parent.beanlifecycle;

public class CustomLifeCycleBean {
    public CustomLifeCycleBean() {
        //System.out.println("CustomLifeCycleBean constructor");
    }

    public void customInitMethod() {
        //System.out.println("method customInitMethod");
    }

    public void customDestroyMethod() {
        //System.out.println("method customDestroyMethod");
    }
}
