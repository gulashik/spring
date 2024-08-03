package com.sprboot.ann.condition.config.springboot;

import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

public class AnyNestedConditionImpl extends AnyNestedCondition/*todo Любое из условий*/ {

    public AnyNestedConditionImpl() {
        super(ConfigurationPhase.PARSE_CONFIGURATION);
    }

    // todo какие условия - удобно через @ConditionalOn*
    @ConditionalOnProperty(value = "conditions.one-condition", havingValue = "true", matchIfMissing = false)
    static class OneCondition {}

    @ConditionalOnProperty(value = "conditions.two-condition", havingValue = "false", matchIfMissing = false)
    static class TwoCondition {}
}
