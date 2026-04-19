package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.adapter.in.web.dto.SurveyTemplateQuestionRequest;
import com.ttcs.backend.adapter.in.web.dto.SurveyTemplateRequest;
import com.ttcs.backend.adapter.out.persistence.questionbank.QuestionBankRepository;
import com.ttcs.backend.adapter.out.persistence.surveytemplate.SurveyTemplateEntity;
import com.ttcs.backend.adapter.out.persistence.surveytemplate.SurveyTemplateRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SurveyTemplateServiceTest {

    @Test
    void shouldCreateTemplateWithQuestionBankLinkedQuestion() {
        SurveyTemplateRepository templateRepository = mock(SurveyTemplateRepository.class);
        QuestionBankRepository questionBankRepository = mock(QuestionBankRepository.class);
        when(questionBankRepository.existsById(11)).thenReturn(true);
        when(templateRepository.save(any(SurveyTemplateEntity.class))).thenAnswer(invocation -> {
            SurveyTemplateEntity entity = invocation.getArgument(0);
            entity.setId(5);
            entity.getQuestions().forEach(question -> question.setId(100 + question.getDisplayOrder()));
            return entity;
        });

        SurveyTemplateService service = new SurveyTemplateService(templateRepository, questionBankRepository);
        var result = service.create(new SurveyTemplateRequest(
                "Course feedback",
                "Reusable course feedback",
                "Course Teaching Feedback",
                "Collect course delivery feedback.",
                "ALL_STUDENTS",
                null,
                List.of(new SurveyTemplateQuestionRequest(11, "Rate clarity", "RATING"))
        ));

        assertEquals(5, result.id());
        assertEquals(1, result.questions().size());
        assertEquals(11, result.questions().getFirst().questionBankEntryId());
    }

    @Test
    void shouldRejectTemplateWithoutQuestions() {
        SurveyTemplateService service = new SurveyTemplateService(
                mock(SurveyTemplateRepository.class),
                mock(QuestionBankRepository.class)
        );

        assertThrows(IllegalArgumentException.class, () -> service.create(new SurveyTemplateRequest(
                "Course feedback",
                null,
                null,
                null,
                "ALL_STUDENTS",
                null,
                List.of()
        )));
    }

    @Test
    void shouldPreserveArchivedStateWhenEditingTemplate() {
        SurveyTemplateRepository templateRepository = mock(SurveyTemplateRepository.class);
        QuestionBankRepository questionBankRepository = mock(QuestionBankRepository.class);
        SurveyTemplateEntity entity = new SurveyTemplateEntity();
        entity.setId(9);
        entity.setName("Archived");
        entity.setRecipientScope("ALL_STUDENTS");
        entity.setActive(false);
        when(templateRepository.findById(9)).thenReturn(Optional.of(entity));
        when(templateRepository.save(any(SurveyTemplateEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SurveyTemplateService service = new SurveyTemplateService(templateRepository, questionBankRepository);
        var result = service.update(9, new SurveyTemplateRequest(
                "Archived edited",
                null,
                null,
                null,
                "ALL_STUDENTS",
                null,
                List.of(new SurveyTemplateQuestionRequest(null, "Rate clarity", "RATING"))
        ));

        assertEquals(false, result.active());
    }
}
