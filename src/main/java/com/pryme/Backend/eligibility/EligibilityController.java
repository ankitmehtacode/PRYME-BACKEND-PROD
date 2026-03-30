package com.pryme.Backend.eligibility;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.pryme.Backend.eligibility.service.EligibilityEngineService;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/public/eligibility/lnt-lap")
@RequiredArgsConstructor
public class EligibilityController {

    private final EligibilityEngineService eligibilityEngineService;

    @PostMapping("/best-match")
    public ResponseEntity<BestMatchResponse> bestMatch(@Valid @RequestBody EligibilityRequest request) {
        return ResponseEntity.ok(eligibilityEngineService.evaluate(request));
    }

    @PostMapping("/nip")
    public ResponseEntity<EligibilityResult> nip(@RequestBody NipRequest request) {
        EligibilityRequest normalized = new EligibilityRequest(
                request.pat(),
                request.depreciation(),
                request.interest(),
                request.existingEmis(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BusinessType.OTHER,
                3,
                2,
                BigDecimal.ZERO,
                12,
                true,
                BigDecimal.ZERO,
                new BigDecimal("12.00"),
                240
        );
        return ResponseEntity.ok(eligibilityEngineService.evaluateNip(normalized));
    }

    @PostMapping("/gst")
    public ResponseEntity<EligibilityResult> gst(@RequestBody GstRequest request) {
        EligibilityRequest normalized = new EligibilityRequest(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                request.existingEmis(),
                BigDecimal.ZERO,
                request.turnover12M(),
                BigDecimal.ZERO,
                request.businessType(),
                3,
                2,
                BigDecimal.ZERO,
                12,
                true,
                BigDecimal.ZERO,
                new BigDecimal("12.00"),
                240
        );
        return ResponseEntity.ok(eligibilityEngineService.evaluateGst(normalized));
    }

    public record NipRequest(BigDecimal pat, BigDecimal depreciation, BigDecimal interest, BigDecimal existingEmis) {}

    public record GstRequest(BigDecimal turnover12M, BusinessType businessType, BigDecimal existingEmis) {}
}
