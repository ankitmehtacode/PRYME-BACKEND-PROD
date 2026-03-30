// File: src/main/java/com/pryme/Backend/eligibility/dto/IncomeComputationInput.java

package com.pryme.Backend.eligibility.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Input for surrogate income calculation.
 * Only the fields relevant to the requested program need to be non-null.
 *
 * Program → required fields:
 *   NIP      : pat, depreciation, interestExpense
 *   Banking  : bankBalanceSamples (preferred) or averageBankBalance
 *   GST      : gstrTurnover12Months, businessType
 *   CashFlow : bankBalanceSamples or averageBankBalance
 *   SENP     : grossReceipts, profession (CS gets multiplier 1.5, others 2.5)
 */
public record IncomeComputationInput(

        // Which surrogate program to evaluate: NIP, Banking, GST, CashFlow, SENP
        String programName,

        // NIP fields
        BigDecimal pat,                     // Profit After Tax (annual)
        BigDecimal depreciation,            // (annual)
        BigDecimal interestExpense,         // (annual)

        // Banking / CashFlow fields
        BigDecimal averageBankBalance,      // pre-computed ABB if samples not provided
        List<BigDecimal> bankBalanceSamples,// raw balances on 5/10/20/25 of each month

        // GST fields
        BigDecimal gstrTurnover12Months,    // last 12-month GSTR-3B turnover
        String businessType,                // Service | Retail | Wholesale | Manufacturing

        // SENP fields
        BigDecimal grossReceipts,           // annual gross receipts
        String profession                   // CA | CS | Doctor
) {}