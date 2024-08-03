package com.sprboot.testcontext.ctxchaching;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ContextCachingOneTest {
    @Autowired
    private ServiceOne serviceOne;

    @Autowired
    private ServiceTwo serviceTwo;

    @Test
    void testContextCachingOne () {
        System.out.println("Test Class - %s | Test Metod - %s | Service - %s | Value - %s".formatted(
                        getClass().getSimpleName(),
                        new Object() {}.getClass().getEnclosingMethod().getName(),
                        serviceOne.getName(),
                        serviceOne.getAndIncState()
                )
        );

        System.out.println("Test Class - %s | Test Metod - %s | Service - %s | Value - %s".formatted(
                        getClass().getSimpleName(),
                        new Object() {}.getClass().getEnclosingMethod().getName(),
                        serviceTwo.getName(),
                        serviceTwo.getAndIncState()
                )
        );
    }

    @Test
    void testContextCachingTwo () {
        System.out.println("Test Class - %s | Test Metod - %s | Service - %s | Value - %s".formatted(
                        getClass().getSimpleName(),
                        new Object() {}.getClass().getEnclosingMethod().getName(),
                        serviceOne.getName(),
                        serviceOne.getAndIncState()
                )
        );

        System.out.println("Test Class - %s | Test Metod - %s | Service - %s | Value - %s".formatted(
                        getClass().getSimpleName(),
                        new Object() {}.getClass().getEnclosingMethod().getName(),
                        serviceTwo.getName(),
                        serviceTwo.getAndIncState()
                )
        );
    }

}
