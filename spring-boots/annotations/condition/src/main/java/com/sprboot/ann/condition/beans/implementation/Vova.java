package com.sprboot.ann.condition.beans.implementation;

import com.sprboot.ann.condition.beans.Person;
import com.sprboot.ann.condition.config.springboot.AllNestedConditionsImpl;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Conditional(AllNestedConditionsImpl.class) // todo от SpringBoot
@Component
public class Vova implements Person {
}
