package com.pryme.Backend.cms;

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
class TestimonialServiceTest {

    @Mock
    private TestimonialRepository repository;

    @InjectMocks
    private TestimonialService service;

    @Test
    void update_throwsNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(id, new TestimonialRequest("Rahul", "Engineer", "Good", 5, true, true, 1)))
                .isInstanceOf(NotFoundException.class);
    }
}
