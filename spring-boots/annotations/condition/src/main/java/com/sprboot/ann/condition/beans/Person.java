package com.sprboot.ann.condition.beans;

public interface Person {
    default void sayHello() {
        System.out.println(String.format("I am %s", getClass().getSimpleName()));
    }
}
