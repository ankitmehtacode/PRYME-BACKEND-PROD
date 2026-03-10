package com.pryme.Backend.bankconfig;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicBankControllerTest {

    @Mock
    private BankService bankService;

    @InjectMocks
    private PublicBankController controller;

    @Test
    void partners_returnsMappedBankList() {
        List<PartnerBankResponse> data = List.of(
                new PartnerBankResponse(UUID.randomUUID(), "Axis Bank", "/logos/axis.svg")
        );
        when(bankService.getActivePartners()).thenReturn(data);

        ResponseEntity<Map<String, List<PartnerBankResponse>>> response = controller.partners();

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).containsEntry("banks", data);
    }
}
