package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.adapter.in.web.dto.QuestionBankRequest;
import com.ttcs.backend.adapter.out.persistence.questionbank.QuestionBankEntity;
import com.ttcs.backend.adapter.out.persistence.questionbank.QuestionBankRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QuestionBankServiceTest {

    @Test
    void shouldCreateReusableQuestionBankEntry() {
        QuestionBankRepository repository = mock(QuestionBankRepository.class);
        when(repository.save(any(QuestionBankEntity.class))).thenAnswer(invocation -> {
            QuestionBankEntity entity = invocation.getArgument(0);
            entity.setId(7);
            return entity;
        });

        QuestionBankService service = new QuestionBankService(repository);
        var result = service.create(new QuestionBankRequest("Rate clarity", "rating", "Teaching"));

        assertEquals(7, result.id());
        assertEquals("Rate clarity", result.content());
        assertEquals("RATING", result.type());
        assertEquals("Teaching", result.category());
    }

    @Test
    void shouldRejectBlankQuestionBankContent() {
        QuestionBankService service = new QuestionBankService(mock(QuestionBankRepository.class));

        assertThrows(IllegalArgumentException.class, () ->
                service.create(new QuestionBankRequest(" ", "RATING", null)));
    }
}
