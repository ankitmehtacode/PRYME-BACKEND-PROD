package com.pryme.Backend.eligibility.audit.certification;

public class CertificationEnums {

    public enum MismatchClassification {
        MASTER_DATA_MISMATCH,
        FORMULA_MISMATCH,
        PROGRAM_SELECTION_MISMATCH,
        RULE_MISMATCH,
        ROUNDING_MISMATCH,
        ENGINE_LOGIC_MISMATCH,
        UNKNOWN
    }

    public enum CertificationGate {
        STRUCTURE_VALIDATION,
        DB_CROSS_REFERENCE,
        RULE_COVERAGE,
        REPLAY_COVERAGE,
        FORMULA_DRIFT_VALIDATION,
        PIPELINE_VERIFICATION,
        SNAPSHOT_DETERMINISM,
        CONDITION_REACHABILITY,
        PRODUCTION_GATE
    }
}
