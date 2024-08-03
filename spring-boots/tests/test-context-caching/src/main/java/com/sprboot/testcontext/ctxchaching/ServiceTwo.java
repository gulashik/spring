package com.sprboot.testcontext.ctxchaching;

import lombok.Data;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Data
@Service
public class ServiceTwo {
    private int state = 1;

    @Getter
    private final String name;

    public Object getAndIncState() {
        return state++;
    }

    public ServiceTwo() {
        name = this.getClass().getSimpleName();
    }
}
