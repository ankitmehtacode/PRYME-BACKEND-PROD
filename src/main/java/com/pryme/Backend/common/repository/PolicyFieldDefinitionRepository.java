package com.pryme.Backend.common.repository;

import com.pryme.Backend.common.entity.PolicyFieldDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyFieldDefinitionRepository extends JpaRepository<PolicyFieldDefinition, Long> {

    Optional<PolicyFieldDefinition> findByFieldKeyAndEntityType(String fieldKey, PolicyFieldDefinition.PolicyEntityType entityType);

    List<PolicyFieldDefinition> findByEntityTypeAndIsActive(PolicyFieldDefinition.PolicyEntityType entityType, boolean isActive);
}
