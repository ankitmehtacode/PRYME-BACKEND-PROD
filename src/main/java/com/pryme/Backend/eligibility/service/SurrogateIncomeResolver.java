// File: src/main/java/com/pryme/Backend/eligibility/service/SurrogateIncomeResolver.java

package com.pryme.Backend.eligibility.service;

import com.pryme.Backend.eligibility.dto.IncomeComputationInput;
import com.pryme.Backend.eligibility.exception.SurrogatePolicyNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Resolves surrogate income for each program.
 *
 * Formulas sourced from Banking_Policies_Template_D1.xlsx:
 *   NIP      : (PAT + Depreciation + Interest) / 12          → monthly
 *   Banking  : Average Bank Balance (ABB of 5/10/20/25 dates) → already monthly
 *   GST      : (12M GSTR-3B Turnover × margin) / 12          → monthly
 *   CashFlow : same as Banking ABB                            → monthly
 *   SENP     : (Gross Receipts × multiplier) / 12             → monthly
 *   SEP      : (Gross Receipts × bank×profession multiplier) / 12 → monthly
 *   CPM_SEP  : ((PAT + Depreciation) × multiplier) / 12      → monthly
 *
 * Returns: monthly income as BigDecimal, DECIMAL128 precision.
 * Caller (EligibilityEngineService) applies FOIR against this monthly figure.
 */
@Service
@Slf4j
public class SurrogateIncomeResolver {

    private static final MathContext MC = MathContext.DECIMAL128;
    private static final BigDecimal TWELVE = new BigDecimal("12");

    // GST profit margins per business type — sourced from Excel
    private static final Map<String, BigDecimal> GST_MARGINS = Map.of(
            "Service",       new BigDecimal("0.10"),
            "Retail",        new BigDecimal("0.12"),
            "Wholesale",     new BigDecimal("0.08"),
            "Manufacturing", new BigDecimal("0.04")
    );

    // ─── SEP MULTIPLIER MATRIX ─────────────────────────────────────────────
    // Key format: "lender:profession" → multiplier
    // For JIO, loan-type matters: "jio:ca:HL" vs "jio:ca:LAP"
    private static final Map<String, BigDecimal> SEP_MULTIPLIERS = new HashMap<>();

    // ─── CPM MULTIPLIER MATRIX ─────────────────────────────────────────────
    // Key format: "lender:profession" → multiplier (currently Yes Bank only)
    private static final Map<String, BigDecimal> CPM_MULTIPLIERS = new HashMap<>();

    static {
        // L&T Finance — HL + LAP (same multipliers)
        SEP_MULTIPLIERS.put("lt:doctor", new BigDecimal("2.5"));
        SEP_MULTIPLIERS.put("lt:ca", new BigDecimal("2.5"));
        SEP_MULTIPLIERS.put("lt:cs", new BigDecimal("1.5"));

        // Bajaj Prime — HL + LAP
        SEP_MULTIPLIERS.put("bajaj:doctor", new BigDecimal("1.5"));
        SEP_MULTIPLIERS.put("bajaj:ca", new BigDecimal("1.5"));

        // Yes Bank — HL + LAP
        SEP_MULTIPLIERS.put("yes:doctor", new BigDecimal("2.0"));
        SEP_MULTIPLIERS.put("yes:bds", new BigDecimal("2.0"));
        SEP_MULTIPLIERS.put("yes:bhms", new BigDecimal("2.0"));
        SEP_MULTIPLIERS.put("yes:bams", new BigDecimal("2.0"));
        SEP_MULTIPLIERS.put("yes:ca", new BigDecimal("1.5"));
        SEP_MULTIPLIERS.put("yes:cs", new BigDecimal("1.5"));
        SEP_MULTIPLIERS.put("yes:architect", new BigDecimal("1.5"));

        // JIO Finance — HL: CA=3.0, Doctor=3.0 ; LAP: CA=2.0, Doctor=3.0
        SEP_MULTIPLIERS.put("jio:doctor", new BigDecimal("3.0"));
        SEP_MULTIPLIERS.put("jio:ca:hl", new BigDecimal("3.0"));
        SEP_MULTIPLIERS.put("jio:ca:lap", new BigDecimal("2.0"));
        SEP_MULTIPLIERS.put("jio:ca", new BigDecimal("3.0")); // fallback if loanType not set

        // TATA Capital — HL + LAP
        SEP_MULTIPLIERS.put("tata:doctor", new BigDecimal("2.5"));
        SEP_MULTIPLIERS.put("tata:ca", new BigDecimal("1.5"));
        SEP_MULTIPLIERS.put("tata:architect", new BigDecimal("1.5"));

        // CPM — Yes Bank only
        CPM_MULTIPLIERS.put("yes:doctor", new BigDecimal("4"));
        CPM_MULTIPLIERS.put("yes:bds", new BigDecimal("4"));
        CPM_MULTIPLIERS.put("yes:bhms", new BigDecimal("4"));
        CPM_MULTIPLIERS.put("yes:bams", new BigDecimal("4"));
        CPM_MULTIPLIERS.put("yes:ca", new BigDecimal("3"));
        CPM_MULTIPLIERS.put("yes:cs", new BigDecimal("3"));
        CPM_MULTIPLIERS.put("yes:architect", new BigDecimal("3"));
    }

    /**
     * Resolves computed monthly income for the given program.
     *
     * @param input  income inputs for the applicant
     * @return       computed monthly income (BigDecimal, never null)
     * @throws SurrogatePolicyNotFoundException if the program name is unrecognised
     */
    public BigDecimal resolve(IncomeComputationInput input) {
        if (input == null || input.programName() == null) {
            throw new SurrogatePolicyNotFoundException(
                    "IncomeComputationInput or programName must not be null");
        }

        BigDecimal monthly = switch (input.programName().toUpperCase()) {
            case "NIP"      -> resolveNip(input);
            case "BANKING"  -> resolveBanking(input);
            case "GST"      -> resolveGst(input);
            case "CASHFLOW" -> resolveCashFlow(input);
            case "SENP"     -> resolveSenp(input);
            case "SEP"      -> resolveSep(input);
            case "CPM_SEP", "CPM SEP", "CPM" -> resolveCpm(input);
            default -> throw new SurrogatePolicyNotFoundException(
                    "No surrogate policy found for program: " + input.programName());
        };

        log.debug("SurrogateIncomeResolver: program={} monthlyIncome={}",
                input.programName(), monthly.setScale(2, RoundingMode.HALF_UP));
        return monthly;
    }

    // ─── NIP: (PAT + Depreciation + Interest) ÷ 12 ────────────────────────
    private BigDecimal resolveNip(IncomeComputationInput input) {
        BigDecimal annual = safe(input.pat())
                .add(safe(input.depreciation()), MC)
                .add(safe(input.interestExpense()), MC);
        return annual.divide(TWELVE, MC);
    }

    // ─── Banking: Average Bank Balance (5/10/20/25 dates), up to 4 accounts ─
    private BigDecimal resolveBanking(IncomeComputationInput input) {
        List<BigDecimal> samples = input.bankBalanceSamples();
        if (samples != null && !samples.isEmpty()) {
            BigDecimal sum = samples.stream()
                    .map(this::safe)
                    .reduce(BigDecimal.ZERO, (a, b) -> a.add(b, MC));
            return sum.divide(BigDecimal.valueOf(samples.size()), MC);
        }
        // Fallback: caller provided a pre-computed ABB
        return safe(input.averageBankBalance());
    }

    // ─── GST: (Last 12M GSTR-3B Turnover × profit margin) ÷ 12 ───────────
    private BigDecimal resolveGst(IncomeComputationInput input) {
        String businessType = input.businessType() != null ? input.businessType() : "";
        BigDecimal margin = GST_MARGINS.getOrDefault(businessType, BigDecimal.ZERO);
        return safe(input.gstrTurnover12Months())
                .multiply(margin, MC)
                .divide(TWELVE, MC);
    }

    // ─── CashFlow: same ABB formula as Banking, LTV deviation handled by engine
    private BigDecimal resolveCashFlow(IncomeComputationInput input) {
        return resolveBanking(input);
    }

    // ─── SENP: (Gross Receipts × multiplier) ÷ 12
    //     Multiplier: CS = 1.5, all others = 2.5  (from Excel)
    private BigDecimal resolveSenp(IncomeComputationInput input) {
        BigDecimal multiplier = "CS".equalsIgnoreCase(input.profession())
                ? new BigDecimal("1.5")
                : new BigDecimal("2.5");
        return safe(input.grossReceipts())
                .multiply(multiplier, MC)
                .divide(TWELVE, MC);
    }

    // ─── SEP: (Gross Receipts × bank×profession multiplier) ÷ 12 ──────────
    //     Multiplier is looked up from SEP_MULTIPLIERS using lender + profession
    //     JIO Finance has a loan-type override for CA (HL=3.0, LAP=2.0)
    private BigDecimal resolveSep(IncomeComputationInput input) {
        String lenderKey = normalizeLenderKey(input.lenderName());
        String professionKey = normalizeProfessionKey(input.profession());
        String loanTypeKey = input.loanType() != null ? input.loanType().toLowerCase() : "";

        // Try loan-type-specific key first (for JIO CA distinction)
        BigDecimal multiplier = SEP_MULTIPLIERS.get(lenderKey + ":" + professionKey + ":" + loanTypeKey);
        if (multiplier == null) {
            multiplier = SEP_MULTIPLIERS.get(lenderKey + ":" + professionKey);
        }
        if (multiplier == null) {
            log.warn("SEP multiplier not found for lender={}, profession={}, loanType={}. Using default 1.5",
                    input.lenderName(), input.profession(), input.loanType());
            multiplier = new BigDecimal("1.5"); // conservative fallback
        }

        log.debug("SEP: lender={}, profession={}, loanType={} → multiplier={}",
                lenderKey, professionKey, loanTypeKey, multiplier);

        return safe(input.grossReceipts())
                .multiply(multiplier, MC)
                .divide(TWELVE, MC);
    }

    // ─── CPM_SEP: ((PAT + Depreciation) × multiplier) ÷ 12 ───────────────
    //     Currently Yes Bank only.
    //     Cap rule: For CA/CS/Architect LAP, result must be ≤ grossReceipts.
    private BigDecimal resolveCpm(IncomeComputationInput input) {
        String lenderKey = normalizeLenderKey(input.lenderName());
        String professionKey = normalizeProfessionKey(input.profession());

        BigDecimal multiplier = CPM_MULTIPLIERS.get(lenderKey + ":" + professionKey);
        if (multiplier == null) {
            log.warn("CPM multiplier not found for lender={}, profession={}. Using default 3",
                    input.lenderName(), input.profession());
            multiplier = new BigDecimal("3"); // conservative fallback
        }

        BigDecimal annualIncome = safe(input.pat())
                .add(safe(input.depreciation()), MC)
                .multiply(multiplier, MC);

        // Cap rule: For CA/CS/Architect on LAP, CPM result ≤ Gross Receipts
        if ("lap".equalsIgnoreCase(input.loanType())
                && (professionKey.equals("ca") || professionKey.equals("cs") || professionKey.equals("architect"))
                && input.grossReceipts() != null
                && annualIncome.compareTo(input.grossReceipts()) > 0) {
            log.info("CPM_SEP: capping annual income {} to grossReceipts {} for LAP {}/{}",
                    annualIncome, input.grossReceipts(), lenderKey, professionKey);
            annualIncome = input.grossReceipts();
        }

        log.debug("CPM_SEP: lender={}, profession={} → multiplier={}, annualIncome={}",
                lenderKey, professionKey, multiplier, annualIncome);

        return annualIncome.divide(TWELVE, MC);
    }

    // ─── LENDER KEY NORMALIZATION ──────────────────────────────────────────
    private String normalizeLenderKey(String lenderName) {
        if (lenderName == null) return "";
        String lower = lenderName.toLowerCase().replaceAll("[^a-z0-9]", "");
        if (lower.contains("lt") || lower.contains("landt")) return "lt";
        if (lower.contains("bajaj")) return "bajaj";
        if (lower.contains("yes")) return "yes";
        if (lower.contains("jio")) return "jio";
        if (lower.contains("tata")) return "tata";
        if (lower.contains("icici")) return "icici";
        if (lower.contains("bandhan")) return "bandhan";
        if (lower.contains("aditya") || lower.contains("abfl")) return "abfl";
        if (lower.contains("baroda") || lower.contains("bob")) return "bob";
        if (lower.contains("sbi")) return "sbi";
        if (lower.contains("hdfc")) return "hdfc";
        if (lower.contains("idfc")) return "idfc";
        if (lower.contains("idbi")) return "idbi";
        return lower;
    }

    // ─── PROFESSION KEY NORMALIZATION ──────────────────────────────────────
    private String normalizeProfessionKey(String profession) {
        if (profession == null) return "";
        String lower = profession.toLowerCase().trim();
        // Direct matches
        if (lower.equals("ca") || lower.equals("chartered accountant")) return "ca";
        if (lower.equals("cs") || lower.equals("company secretary")) return "cs";
        if (lower.equals("doctor") || lower.equals("mbbs") || lower.equals("md")) return "doctor";
        if (lower.equals("bds")) return "bds";
        if (lower.equals("bhms")) return "bhms";
        if (lower.equals("bams")) return "bams";
        if (lower.equals("architect")) return "architect";
        return lower;
    }

    private BigDecimal safe(BigDecimal v)
    {

        return v != null ? v : BigDecimal.ZERO;
    }
}

