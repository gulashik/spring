package com.sprboot.ann.condition.config.springboot;

import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

public class AllNestedConditionsImpl extends AllNestedConditions/*todo Все условия*/ {
    public AllNestedConditionsImpl() {
        super(ConfigurationPhase.PARSE_CONFIGURATION);
    }

    // todo какие условия - удобно через @ConditionalOn*
    @ConditionalOnProperty(value = "conditions.one-condition", havingValue = "true", matchIfMissing = false)
    static class OneCondition {}

    @ConditionalOnProperty(value = "conditions.two-condition", havingValue = "true", matchIfMissing = false)
    static class TwoCondition {}
}
