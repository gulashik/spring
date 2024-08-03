package com.sprboot.ann.condition.beans.implementation;

import com.sprboot.ann.condition.beans.Person;
import com.sprboot.ann.condition.config.springboot.AnyNestedConditionImpl;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Conditional(AnyNestedConditionImpl.class) // todo от SpringBoot
@Component
public class Vita implements Person {
}
