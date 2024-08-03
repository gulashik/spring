package com.sprboot.testcontext.ctxchaching;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD) // todo явно указываем пересоздать контекст = контекст другой
//@ActiveProfiles("test") // todo изменяет профили контекста = контекст другой
//@TestPropertySource("classpath:application-test.properties") todo добавляем проперти = контекст другой
//@SpringBootTest(classes = {ServiceOne.class, ServiceTwo.class, ServiceThr.class}) // todo изменяем набор bean-в
@SpringBootTest
class ContextCachingTwoTest {
    @Autowired
    private ServiceOne serviceOne;

    @Autowired
    private ServiceTwo serviceTwo;

    //@MockBean // todo изменяет bean = контекст другой
    @Autowired
    private ServiceThr serviceThr;

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
    // @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
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
