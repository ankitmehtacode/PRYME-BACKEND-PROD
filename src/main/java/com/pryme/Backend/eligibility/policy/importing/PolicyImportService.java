package com.pryme.Backend.eligibility.policy.importing;

import com.pryme.Backend.eligibility.policy.compilation.PolicyCompiler;
import com.pryme.Backend.eligibility.policy.model.*;
import com.pryme.Backend.eligibility.policy.repository.DatabasePolicyRepository;
import com.pryme.Backend.eligibility.policy.repository.MemoryPolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyImportService {

    private final PolicySourceAdapter adapter;
    private final DatabasePolicyRepository databasePolicyRepository;
    private final MemoryPolicyRepository memoryPolicyRepository;
    private final PolicyCompiler compiler = new PolicyCompiler();

    public PolicyBundle importAndFreeze() {
        log.info("Initiating import of PolicyBundle...");
        PolicySourceInput sourceInput = adapter.loadSource();
        
        PolicyBundle compiled = compiler.compile(sourceInput, "2026.07.05", "Operations");

        // Save to Database projection metadata
        databasePolicyRepository.save(compiled);

        // Register in Memory store
        memoryPolicyRepository.save(compiled);

        return compiled;
    }
}
