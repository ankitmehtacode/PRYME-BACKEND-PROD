package com.pryme.Backend.bankconfig;

import com.pryme.Backend.common.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankServiceTest {

    @Mock
    private BankRepository bankRepository;

    @InjectMocks
    private BankService bankService;

    @Test
    void update_throwsNotFoundWhenBankMissing() {
        UUID id = UUID.randomUUID();
        when(bankRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bankService.update(id, new BankRequest("Bank", "/logo.png", true)))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Bank footprint not found");
    }
}
