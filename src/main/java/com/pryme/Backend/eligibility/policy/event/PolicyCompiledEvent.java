package com.pryme.Backend.eligibility.policy.event;

import com.pryme.Backend.eligibility.policy.model.PolicyBundle;
import org.springframework.context.ApplicationEvent;

public class PolicyCompiledEvent extends ApplicationEvent {
    private final PolicyBundle bundle;

    public PolicyCompiledEvent(Object source, PolicyBundle bundle) {
        super(source);
        this.bundle = bundle;
    }

    public PolicyBundle getBundle() {
        return bundle;
    }
}
