package com.pryme.Backend.iam;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserIdGeneratorService {

    private final UserRepository userRepository;

    @Transactional
    public synchronized void ensureUserIds(User user) {
        String stateCode = user.getState();
        if (stateCode == null || stateCode.isBlank()) {
            stateCode = "XX";
        } else {
            stateCode = mapStateToCode(stateCode);
        }

        if (user.getRole() == Role.USER) {
            // Customer ID: PRY-CUS-[STATE]-0000001
            if (user.getCustomerId() == null || !user.getCustomerId().startsWith("PRY-CUS-" + stateCode)) {
                user.setCustomerId(generateNextCustomerId(stateCode));
            }
            user.setEmployeeId(null);
        } else {
            // Employee ID: PRY-EMP-[STATE]-000001
            if (user.getEmployeeId() == null || !user.getEmployeeId().startsWith("PRY-EMP-" + stateCode)) {
                user.setEmployeeId(generateNextEmployeeId(stateCode));
            }
            user.setCustomerId(null);
        }
    }

    private String generateNextCustomerId(String stateCode) {
        String pattern = "PRY-CUS-" + stateCode + "%";
        List<String> ids = userRepository.findCustomerIdsByPattern(pattern);
        long maxNum = 0;
        for (String id : ids) {
            try {
                String prefix = "PRY-CUS-" + stateCode;
                if (id != null && id.startsWith(prefix) && id.length() > prefix.length()) {
                    String numStr = id.substring(prefix.length());
                    long num = Long.parseLong(numStr);
                    if (num > maxNum) {
                        maxNum = num;
                    }
                }
            } catch (Exception ignored) {}
        }
        long nextNum = maxNum + 1;
        return String.format("PRY-CUS-%s%07d", stateCode, nextNum);
    }

    private String generateNextEmployeeId(String stateCode) {
        String pattern = "PRY-EMP-" + stateCode + "%";
        List<String> ids = userRepository.findEmployeeIdsByPattern(pattern);
        long maxNum = 0;
        for (String id : ids) {
            try {
                String prefix = "PRY-EMP-" + stateCode;
                if (id != null && id.startsWith(prefix) && id.length() > prefix.length()) {
                    String numStr = id.substring(prefix.length());
                    long num = Long.parseLong(numStr);
                    if (num > maxNum) {
                        maxNum = num;
                    }
                }
            } catch (Exception ignored) {}
        }
        long nextNum = maxNum + 1;
        return String.format("PRY-EMP-%s%06d", stateCode, nextNum);
    }

    public String mapStateToCode(String state) {
        if (state == null || state.isBlank()) return "XX";
        String s = state.trim().toUpperCase(Locale.ROOT);
        if (s.length() == 2) {
            return s;
        }
        return switch (s) {
            case "MAHARASHTRA" -> "MH";
            case "DELHI" -> "DL";
            case "KARNATAKA" -> "KA";
            case "TAMIL NADU" -> "TN";
            case "GUJARAT" -> "GJ";
            case "RAJASTHAN" -> "RJ";
            case "UTTAR PRADESH" -> "UP";
            case "WEST BENGAL" -> "WB";
            case "ANDHRA PRADESH" -> "AP";
            case "TELANGANA" -> "TS";
            case "KERALA" -> "KL";
            case "PUNJAB" -> "PB";
            case "HARYANA" -> "HR";
            case "MADHYA PRADESH" -> "MP";
            case "BIHAR" -> "BR";
            case "GOA" -> "GA";
            default -> s.substring(0, Math.min(2, s.length()));
        };
    }
}
