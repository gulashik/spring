package com.sprboot.testcontext.ctxchaching;

import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
public class ServiceOne {
    private int state = 1;
    @Getter
    private final String name;

    public Object getAndIncState() {
        return state++;
    }

    public ServiceOne() {
        name = this.getClass().getSimpleName();
    }
}
