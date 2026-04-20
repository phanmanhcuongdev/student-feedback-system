package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.model.QuestionBankEntry;
import com.ttcs.backend.application.domain.model.SurveyTemplate;
import com.ttcs.backend.application.domain.model.SurveyTemplateQuestion;
import com.ttcs.backend.application.port.in.admin.SurveyTemplateCommand;
import com.ttcs.backend.application.port.in.admin.SurveyTemplateQuestionCommand;
import com.ttcs.backend.application.port.out.admin.ManageQuestionBankPort;
import com.ttcs.backend.application.port.out.admin.ManageSurveyTemplatePort;
import com.ttcs.backend.application.port.out.admin.QuestionBankSearchPage;
import com.ttcs.backend.application.port.out.admin.QuestionBankSearchQuery;
import com.ttcs.backend.application.port.out.admin.SurveyTemplateSearchPage;
import com.ttcs.backend.application.port.out.admin.SurveyTemplateSearchQuery;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SurveyTemplateServiceTest {

    @Test
    void shouldCreateTemplateWithQuestionBankLinkedQuestion() {
        InMemorySurveyTemplatePort templatePort = new InMemorySurveyTemplatePort();
        InMemoryQuestionBankPort questionBankPort = new InMemoryQuestionBankPort(11);
        SurveyTemplateService service = new SurveyTemplateService(templatePort, questionBankPort);
        var result = service.create(new SurveyTemplateCommand(
                "Course feedback",
                "Reusable course feedback",
                "Course Teaching Feedback",
                "Collect course delivery feedback.",
                "ALL_STUDENTS",
                null,
                List.of(new SurveyTemplateQuestionCommand(11, "Rate clarity", "RATING"))
        ));

        assertEquals(1, result.id());
        assertEquals(1, result.questions().size());
        assertEquals(11, result.questions().getFirst().questionBankEntryId());
    }

    @Test
    void shouldRejectTemplateWithoutQuestions() {
        SurveyTemplateService service = new SurveyTemplateService(
                new InMemorySurveyTemplatePort(),
                new InMemoryQuestionBankPort()
        );

        assertThrows(IllegalArgumentException.class, () -> service.create(new SurveyTemplateCommand(
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
        InMemorySurveyTemplatePort templatePort = new InMemorySurveyTemplatePort();
        InMemoryQuestionBankPort questionBankPort = new InMemoryQuestionBankPort();
        templatePort.save(new SurveyTemplate(
                9,
                "Archived",
                null,
                null,
                null,
                "ALL_STUDENTS",
                null,
                false,
                LocalDateTime.now(),
                null,
                List.of()
        ));

        SurveyTemplateService service = new SurveyTemplateService(templatePort, questionBankPort);
        var result = service.update(9, new SurveyTemplateCommand(
                "Archived edited",
                null,
                null,
                null,
                "ALL_STUDENTS",
                null,
                List.of(new SurveyTemplateQuestionCommand(null, "Rate clarity", "RATING"))
        ));

        assertEquals(false, result.active());
    }

    @Test
    void shouldRejectArchivedTemplateApply() {
        InMemorySurveyTemplatePort templatePort = new InMemorySurveyTemplatePort();
        templatePort.save(new SurveyTemplate(
                3,
                "Archived",
                null,
                null,
                null,
                "ALL_STUDENTS",
                null,
                false,
                LocalDateTime.now(),
                null,
                List.of(new SurveyTemplateQuestion(null, null, "Rate clarity", "RATING", 0))
        ));
        SurveyTemplateService service = new SurveyTemplateService(templatePort, new InMemoryQuestionBankPort());

        assertThrows(IllegalArgumentException.class, () -> service.apply(3));
    }

    @Test
    void shouldRejectMissingQuestionBankEntry() {
        SurveyTemplateService service = new SurveyTemplateService(
                new InMemorySurveyTemplatePort(),
                new InMemoryQuestionBankPort()
        );

        assertThrows(IllegalArgumentException.class, () -> service.create(new SurveyTemplateCommand(
                "Course feedback",
                null,
                null,
                null,
                "ALL_STUDENTS",
                null,
                List.of(new SurveyTemplateQuestionCommand(99, "Rate clarity", "RATING"))
        )));
    }

    private static final class InMemorySurveyTemplatePort implements ManageSurveyTemplatePort {
        private final List<SurveyTemplate> templates = new ArrayList<>();
        private int nextId = 1;

        @Override
        public SurveyTemplateSearchPage loadPage(SurveyTemplateSearchQuery query) {
            return new SurveyTemplateSearchPage(templates, query.page(), query.size(), templates.size(), templates.isEmpty() ? 0 : 1);
        }

        @Override
        public Optional<SurveyTemplate> loadById(Integer id) {
            return templates.stream().filter(template -> template.id().equals(id)).findFirst();
        }

        @Override
        public SurveyTemplate save(SurveyTemplate template) {
            Integer id = template.id() == null ? nextId++ : template.id();
            SurveyTemplate saved = new SurveyTemplate(
                    id,
                    template.name(),
                    template.description(),
                    template.suggestedTitle(),
                    template.suggestedSurveyDescription(),
                    template.recipientScope(),
                    template.recipientDepartmentId(),
                    template.active(),
                    template.createdAt(),
                    template.updatedAt(),
                    withQuestionIds(template.questions())
            );
            for (int index = 0; index < templates.size(); index++) {
                if (templates.get(index).id().equals(saved.id())) {
                    templates.set(index, saved);
                    return saved;
                }
            }
            templates.add(saved);
            return saved;
        }

        private List<SurveyTemplateQuestion> withQuestionIds(List<SurveyTemplateQuestion> questions) {
            return questions.stream()
                    .map(question -> new SurveyTemplateQuestion(
                            question.id() == null ? 100 + question.displayOrder() : question.id(),
                            question.questionBankEntryId(),
                            question.content(),
                            question.type(),
                            question.displayOrder()
                    ))
                    .toList();
        }
    }

    private static final class InMemoryQuestionBankPort implements ManageQuestionBankPort {
        private final List<Integer> existingIds;

        private InMemoryQuestionBankPort(Integer... existingIds) {
            this.existingIds = List.of(existingIds);
        }

        @Override
        public QuestionBankSearchPage loadPage(QuestionBankSearchQuery query) {
            return new QuestionBankSearchPage(List.of(), 0, 0, 0, 0);
        }

        @Override
        public Optional<QuestionBankEntry> loadById(Integer id) {
            if (!existingIds.contains(id)) {
                return Optional.empty();
            }
            return Optional.of(new QuestionBankEntry(id, "Rate clarity", "RATING", null, true, LocalDateTime.now(), null));
        }

        @Override
        public QuestionBankEntry save(QuestionBankEntry entry) {
            return entry;
        }
    }
}
