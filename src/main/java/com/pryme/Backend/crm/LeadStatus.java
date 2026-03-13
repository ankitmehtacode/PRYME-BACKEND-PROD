package com.pryme.Backend.crm;

public enum LeadStatus {
    NEW,
    CONTACTED,
    // 🧠 PRODUCTION FIX: Prevents the Elevation Engine from failing during compilation
    CONVERTED,
    REJECTED
}