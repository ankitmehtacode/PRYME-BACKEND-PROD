package com.pryme.Backend.eligibility.service;
import com.pryme.Backend.eligibility.dto.PreflightRequest;
import com.pryme.Backend.eligibility.dto.PreflightResult;

public interface GeneralPolicyPreflightService {
    PreflightResult evaluate(PreflightRequest request);
}