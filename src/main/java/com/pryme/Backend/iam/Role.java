package com.pryme.Backend.iam;

public enum Role
{
    SUPER_ADMIN, // Full CMS control [cite: 87]
    ADMIN,       // Regional control [cite: 87]
    EMPLOYEE,    // Can view leads, cannot delete banks [cite: 87]
    USER         // The public applicant [cite: 86]
}