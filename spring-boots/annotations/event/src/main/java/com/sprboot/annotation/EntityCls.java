package com.sprboot.annotation;

// Entity - Сущность для события
public class EntityCls {
    private final String name;

    public EntityCls(String name) {
        this.name = name;
    }

    public String toString() {
        return "EntityCls(name=" + this.name + ")";
    }
}
