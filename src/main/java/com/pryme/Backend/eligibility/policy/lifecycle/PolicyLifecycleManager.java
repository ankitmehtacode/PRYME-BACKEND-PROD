package com.pryme.Backend.eligibility.policy.lifecycle;

import com.pryme.Backend.eligibility.policy.model.PolicyState;
import com.pryme.Backend.eligibility.policy.projection.PolicyBundleEntity;
import com.pryme.Backend.eligibility.policy.repository.PolicyBundleEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyLifecycleManager {

    private final PolicyBundleEntityRepository repository;

    public void transitionTo(String bundleId, PolicyState targetState) {
        PolicyBundleEntity entity = repository.findByBundleId(bundleId)
            .orElseThrow(() -> new IllegalArgumentException("Bundle not found: " + bundleId));

        PolicyState currentState = PolicyState.valueOf(entity.getState());
        if (isValidTransition(currentState, targetState)) {
            entity.setState(targetState.name());
            repository.save(entity);
            log.info("Transitioned bundle {} from {} to {}", bundleId, currentState, targetState);
        } else {
            throw new IllegalStateException("Invalid transition from " + currentState + " to " + targetState);
        }
    }

    private boolean isValidTransition(PolicyState current, PolicyState target) {
        if (current == target) return true;
        switch (current) {
            case DRAFT:
                return target == PolicyState.VALIDATING || target == PolicyState.CERTIFIED;
            case VALIDATING:
                return target == PolicyState.CERTIFIED || target == PolicyState.DRAFT;
            case CERTIFIED:
                return target == PolicyState.APPROVED || target == PolicyState.ACTIVE;
            case APPROVED:
                return target == PolicyState.DEPLOYED || target == PolicyState.ACTIVE;
            case DEPLOYED:
                return target == PolicyState.ACTIVE;
            case ACTIVE:
                return target == PolicyState.ARCHIVED || target == PolicyState.ROLLED_BACK;
            default:
                return false;
        }
    }
}
