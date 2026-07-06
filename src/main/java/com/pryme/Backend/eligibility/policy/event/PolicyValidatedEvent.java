package com.pryme.Backend.eligibility.policy.event;

import com.pryme.Backend.eligibility.policy.model.PolicyBundle;
import com.pryme.Backend.eligibility.policy.validation.PolicyValidationResult;
import org.springframework.context.ApplicationEvent;

public class PolicyValidatedEvent extends ApplicationEvent {
    private final PolicyBundle bundle;
    private final PolicyValidationResult result;

    public PolicyValidatedEvent(Object source, PolicyBundle bundle, PolicyValidationResult result) {
        super(source);
        this.bundle = bundle;
        this.result = result;
    }

    public PolicyBundle getBundle() {
        return bundle;
    }

    public PolicyValidationResult getResult() {
        return result;
    }
}
