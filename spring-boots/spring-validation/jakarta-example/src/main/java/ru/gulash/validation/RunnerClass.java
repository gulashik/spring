package ru.gulash.validation;


import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import ru.gulash.validation.entity.ChildEntity;
import ru.gulash.validation.entity.ParentEntity;
import ru.gulash.validation.entity.group.DefaultReasonOfValidation;
import ru.gulash.validation.entity.group.ParentReasonOfValidation;

import java.util.Set;

@RequiredArgsConstructor
@Component
public class RunnerClass implements ApplicationRunner {
    // todo нужен jakarta.validation.Validator
    private final Validator validator;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        // todo метод validate - получаем множество нарушений
        Set<ConstraintViolation<ParentEntity>> violations =
                validator.validate(
                        // Что проверяем
                        new ParentEntity(/*name*/null, /*name2*/null, /*child*/new ChildEntity()),
                        // группы, валидации которых проверяем
                        ParentReasonOfValidation.class,
                        DefaultReasonOfValidation.class
                );
        // todo выбрасываем исключение
        /*if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);

        }*/
        // todo метод validateProperty - проверка конкретного аттрибута
        Set<ConstraintViolation<ParentEntity>> validateProperty = validator.validateProperty(
                // Что проверяем
                new ParentEntity(/*name*/null, /*name2*/null, /*child*/new ChildEntity()),
                "name", // имя аттрибута
                // группы, валидации которых проверяем
                ParentReasonOfValidation.class,
                DefaultReasonOfValidation.class
        );
        // todo метод validateValue - проверка конкретного аттрибута по правилу другого
        //  violation будет из используемого атрибута(не понял зачем так)
        Set<ConstraintViolation<ParentEntity>> validateValue = validator.validateValue(
                // Тип откуда взять проверку(из этого же класса)
                ParentEntity.class,
                // использовать проверку из аттрибута
                "name",
                // достать аттрибут проверки
                new ParentEntity(/*name*/null, /*name2*/null, /*child*/new ChildEntity()).getSomeAttrWithOutAnnotation(),
                // группы, валидации которых проверяем
                ParentReasonOfValidation.class,
                DefaultReasonOfValidation.class
        );

        // todo можно собирать нарушения
        violations.addAll(validateValue);
        violations.addAll(validateProperty);

        // todo вывод всех
        violations.forEach(System.out::println);
        // ConstraintViolationImpl{
        //  interpolatedMessage='My Child Violation message', propertyPath=child.name,
        //  rootBeanClass=class ru.gulash.validation.entity.ParentEntity, messageTemplate='My Child Violation message'
        //  } ....

        // todo одно наущение
        ConstraintViolation<ParentEntity> violation =
                violations.stream()
                        .filter( curViolation -> curViolation.getPropertyPath().toString().equals("child.name") )
                        .limit(1)
                        .toList().get(0);
        // todo атрибуты
        System.out.println( "attribute: " + violation.getPropertyPath() );
            // attribute: name
        System.out.println( "violation message: " + violation.getMessage() );
            // violation message: My Violation message
        System.out.println( "violation annotation message: " + violation.getMessageTemplate() );
            // violation annotation message: My Violation message
        System.out.println( "root bean instance: " + violation.getRootBean() );
            // верхний(откуда стартует проверка) bean instance: root bean instance: ParentEntity(name=null, child=ChildEntity(name=null))
        System.out.println( "leaf bean instance: " + violation.getLeafBean() );
            // листовой(где ошибка) bean instance: leaf bean instance: ChildEntity(name=null)
    }
}