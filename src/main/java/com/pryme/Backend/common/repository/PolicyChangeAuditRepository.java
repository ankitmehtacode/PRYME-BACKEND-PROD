package com.pryme.Backend.common.repository;

import com.pryme.Backend.common.entity.PolicyChangeAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyChangeAuditRepository extends JpaRepository<PolicyChangeAudit, Long> {

    List<PolicyChangeAudit> findByEntityTypeAndEntityIdOrderByAppliedAtDesc(String entityType, Long entityId);

    Page<PolicyChangeAudit> findByChangedByUserIdOrderByAppliedAtDesc(Long userId, Pageable pageable);

    Page<PolicyChangeAudit> findByFieldKeyOrderByAppliedAtDesc(String fieldKey, Pageable pageable);
}
