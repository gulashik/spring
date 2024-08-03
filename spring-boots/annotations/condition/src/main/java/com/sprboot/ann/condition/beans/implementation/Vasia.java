package com.sprboot.ann.condition.beans.implementation;

import com.sprboot.ann.condition.beans.Person;
import com.sprboot.ann.condition.config.spring.ConditionImpl;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Conditional(ConditionImpl.class) // todo от Spring
@Component
public class Vasia implements Person {
}
