package com.pryme.Backend.eligibility.resolver;
import com.pryme.Backend.eligibility.dto.IncomeComputationInput;
import com.pryme.Backend.eligibility.exception.SurrogatePolicyNotFoundException;
import java.math.BigDecimal;

public interface SurrogateIncomeResolver {
    BigDecimal resolve(IncomeComputationInput input) throws SurrogatePolicyNotFoundException;
}
