package ru.gulash.bean.collection.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeansConfig {

    @Bean
    BeanCls getBean1() {
        return new BeanCls("Bean-1");
    }

    @Bean
    BeanCls getBean2() {
        return new BeanCls("Bean-2");
    }

    @Bean
    BeanCls getBean3() {
        return new BeanCls("Bean-3");
    }
}
