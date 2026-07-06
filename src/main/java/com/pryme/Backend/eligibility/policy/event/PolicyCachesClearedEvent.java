package com.pryme.Backend.eligibility.policy.event;

import org.springframework.context.ApplicationEvent;

public class PolicyCachesClearedEvent extends ApplicationEvent {
    public PolicyCachesClearedEvent(Object source) {
        super(source);
    }
}
