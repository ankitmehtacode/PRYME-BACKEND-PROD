package com.pryme.Backend.crm;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ApplicationStatusHistoryRepository extends JpaRepository<ApplicationStatusHistory, UUID> {
}