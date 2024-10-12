package com.sprboot.parent.beanlifecycle;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class BeanLifecycleApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(BeanLifecycleApplication.class, args);

//        System.out.println("/////////////////////////////////");
//        for (String name : context.getBeanDefinitionNames()) {
//            System.out.println(name);
//        }
//        System.out.println("/////////////////////////////////");
    }
}

@Configuration
class Config {
    @Bean(initMethod = "initCall", destroyMethod = "destroyCall") // Метод будет дернут при инициализации и окончании
    public Person personMary() {
        return new Person("Mary");
    }
}

/*Регистрация дополнительных bean-ов--------------*/
@Import(ConfigImportBeanDefinitionRegistrar.class)
@Configuration
class SomeConfig {}
// указываем в @Import-e см. выше
class ConfigImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar/*регистрация дополнительных bean-ов*/{
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry, BeanNameGenerator importBeanNameGenerator) {
        System.out.println("registerBeanDefinitions method from ImportBeanDefinitionRegistrar - Зарегистрировали новый Bean");

        GenericBeanDefinition gbd = new GenericBeanDefinition(); // шаблонный bean
        gbd.setBeanClass(CustomLifeCycleBean.class); // из чего делаем

        // указываем методы
        gbd.setInitMethodName("customInitMethod");
        gbd.setDestroyMethodName("customDestroyMethod");

        // регистрируем
        registry.registerBeanDefinition("customLifeCycleBean", gbd);
    }
}

/*Модификация Bean definition-ов*/
/*Модификация Bean-ов*/
@Configuration
class ConfigAllBeans implements
        BeanFactoryPostProcessor/*модификация bean definition-ов*/,
        BeanPostProcessor/*ВСЕ Bean-ы будут поданы на вход ПЕРЕД и ПОСЛЕ инициализации*/
{
    @Override // BeanFactoryPostProcessor
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // Лень причёсывать пример. Меняем definition одного класса на другой
        //  private static final String CLASS_NAME_ATTR = "className";
        /*        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            var d = beanFactory.getBeanDefinition(beanName);
            if (d instanceof ScannedGenericBeanDefinition scannedBeanDefinition) {

                if (!GirlfiendPhoneNumber.class.getName().equalsIgnoreCase(d.getBeanClassName())) {
                    continue;
                }
                d.setBeanClassName(FriendPhoneNumber.class.getName());
                var classNameAttr = new BeanMetadataAttribute(CLASS_NAME_ATTR, FriendPhoneNumber.class.getName());
                scannedBeanDefinition.addMetadataAttribute(classNameAttr);
            }
        }*/
        System.out.println("postProcessBeanFactory method BeanPostProcessor - Модификация Bean definition-ов");
    }

    @Override // BeanPostProcessor
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        /*Модификация Bean-ов*/
        if(beanName.equals("personMary")) {
            System.out.println("BeanPostProcessor BeforeInitialization = " + beanName + " - Модификация Bean-ов");
        }
        return BeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
    }

    @Override // BeanPostProcessor
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        /*Модификация Bean-ов*/
        if(beanName.equals("personMary")) {
            System.out.println("BeanPostProcessor AfterInitialization = " + beanName + " - Модификация Bean-ов");
        }
        // Пример лень причёсывать
        /*if (bean.getClass().isAssignableFrom(Phone.class)) {
            Class<?> aClass = Phone.class;
            try {
                Field greetingField = aClass.getDeclaredField(GREETING_PROPERTY);

                greetingField.setAccessible(true);
                greetingField.set(bean, "Ай-да в гараж. Стихи читать!");
            } catch (Exception e) {
                throw new InvalidPropertyException(Phone.class, GREETING_PROPERTY,
                        "Bean class does not have expected property", e);
            }
        }
        return bean;*/
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}

class Person
        implements
        BeanNameAware/*назначение имени*/,
        EnvironmentAware/*получаем по environment*/,
        ApplicationContextAware/*создание ApplicationContext*/,
        InitializingBean/*инициализация*/,
        DisposableBean/*уничтожение*/,
        ApplicationListener<ContextRefreshedEvent>/*Слушаем один из Event-ов*/
        /*ContextRefreshedEvent(удобней всего),ContextStartedEvent,ContextStoppedEvent,ContextClosedEvent*/
{

    private String name;

    public Person(String name) {
        this.name = name;
    }

    public void initCall() {
        System.out.println("@Bean(initMethod = \"initCall\") - initCall method");
    }

    public void destroyCall() {
        System.out.println("@Bean(destroyMethod = \"destroyCall\") - destroyCall method");
    }

    @PostConstruct // Инициализация
    public void init() {
        System.out.println("@PostConstruct - init method ");
    }

    @PreDestroy // Уничтожение
    public void destroyAnotation() {
        System.out.println("@PreDestroy - destroyAnotation method");
    }

    @Override // InitializingBean
    public void afterPropertiesSet() {
        System.out.println("afterPropertiesSet method from InitializingBean");
    }

    @Override // DisposableBean
    public void destroy() {
        System.out.println("destroy method from DisposableBean");
    }

    @Override // BeanNameAware
    public void setBeanName(String name) {
        System.out.println("setBeanName method from BeanNameAware");
    }

    @Override // EnvironmentAware
    public void setEnvironment(Environment environment) {
        // environment.getActiveProfiles();
        System.out.println("setEnvironment method from EnvironmentAware");
    }

    @Override // ApplicationContextAware
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        System.out.println("setApplicationContext method from ApplicationContextAware");
    }

    @Override // ApplicationListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        System.out.println("onApplicationEvent method from ApplicationListener; Context available; Event-" + event.getClass().getSimpleName() );
        // можем получить контекст
        ApplicationContext applicationContext = event.getApplicationContext();
    }
}


@Component
/*public*/ class MyContextRefreshedEventHandler {
    // Начиная с Spring 4.2, вместо реализации ApplicationListener можно использовать аннотацию @EventListener для упрощения кода
    @EventListener
    public void handleContextRefreshed(ContextRefreshedEvent event) {
        System.out.println("Контекст приложения обновлен или инициализирован (через @EventListener)!");
        // логика здесь
    }
}
