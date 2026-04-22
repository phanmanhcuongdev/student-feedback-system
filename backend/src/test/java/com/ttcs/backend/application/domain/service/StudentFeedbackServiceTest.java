package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.model.Department;
import com.ttcs.backend.application.domain.model.Feedback;
import com.ttcs.backend.application.domain.model.FeedbackResponse;
import com.ttcs.backend.application.domain.model.Role;
import com.ttcs.backend.application.domain.model.Status;
import com.ttcs.backend.application.domain.model.Student;
import com.ttcs.backend.application.domain.model.User;
import com.ttcs.backend.application.port.in.feedback.command.CreateFeedbackCommand;
import com.ttcs.backend.application.port.in.feedback.GetStudentFeedbackQuery;
import com.ttcs.backend.application.port.in.feedback.command.RespondToFeedbackCommand;
import com.ttcs.backend.application.port.in.feedback.result.CreateFeedbackResult;
import com.ttcs.backend.application.port.in.feedback.result.RespondToFeedbackResult;
import com.ttcs.backend.application.port.in.feedback.result.StudentFeedbackResult;
import com.ttcs.backend.application.port.out.LoadFeedbackPort;
import com.ttcs.backend.application.port.out.LoadFeedbackQuery;
import com.ttcs.backend.application.port.out.LoadFeedbackResponsePort;
import com.ttcs.backend.application.port.out.LoadStudentPort;
import com.ttcs.backend.application.port.out.StaffFeedbackSearchPage;
import com.ttcs.backend.application.port.out.StudentFeedbackSearchItem;
import com.ttcs.backend.application.port.out.StudentFeedbackSearchPage;
import com.ttcs.backend.application.port.out.SaveFeedbackPort;
import com.ttcs.backend.application.port.out.SaveFeedbackResponsePort;
import com.ttcs.backend.application.port.out.ai.SendTranslationTaskPort;
import com.ttcs.backend.application.port.out.ai.TranslationTaskCommand;
import com.ttcs.backend.application.port.out.auth.LoadUserByIdPort;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudentFeedbackServiceTest {

    @Test
    void shouldCreateFeedbackWhenInputIsValid() {
        RecordingFeedbackPort feedbackPort = new RecordingFeedbackPort();
        RecordingFeedbackResponsePort responsePort = new RecordingFeedbackResponsePort();
        RecordingTranslationTaskPort translationTaskPort = new RecordingTranslationTaskPort();
        Student student = student();
        StudentFeedbackService service = new StudentFeedbackService(
                studentId -> Optional.of(student),
                feedbackPort,
                responsePort,
                userId -> Optional.of(admin()),
                feedbackPort,
                responsePort,
                translationTaskPort
        );

        CreateFeedbackResult result = service.createFeedback(
                new CreateFeedbackCommand(student.getId(), "Navigation issue", "Please simplify the sidebar.")
        );

        assertTrue(result.success());
        assertEquals("FEEDBACK_CREATED", result.code());
        assertEquals(1, feedbackPort.saved.size());
        Feedback savedFeedback = feedbackPort.saved.getFirst();
        assertEquals("Please simplify the sidebar.", savedFeedback.getContent());
        assertEquals("Please simplify the sidebar.", savedFeedback.getContentOriginal());
        assertFalse(savedFeedback.isAutoTranslated());
        assertEquals(1, translationTaskPort.sent.size());
        TranslationTaskCommand task = translationTaskPort.sent.getFirst();
        assertEquals(savedFeedback.getId(), task.entityId());
        assertEquals("FEEDBACK", task.entityType());
        assertEquals(savedFeedback.getContentOriginal(), task.content());
    }

    @Test
    void shouldRejectBlankFeedback() {
        RecordingFeedbackPort feedbackPort = new RecordingFeedbackPort();
        RecordingFeedbackResponsePort responsePort = new RecordingFeedbackResponsePort();
        StudentFeedbackService service = new StudentFeedbackService(
                studentId -> Optional.of(student()),
                feedbackPort,
                responsePort,
                userId -> Optional.of(admin()),
                feedbackPort,
                responsePort,
                new RecordingTranslationTaskPort()
        );

        CreateFeedbackResult result = service.createFeedback(
                new CreateFeedbackCommand(1, "   ", "   ")
        );

        assertFalse(result.success());
        assertEquals("INVALID_INPUT", result.code());
    }

    @Test
    void shouldListStudentFeedbackWithResponses() {
        RecordingFeedbackPort feedbackPort = new RecordingFeedbackPort();
        RecordingFeedbackResponsePort responsePort = new RecordingFeedbackResponsePort();
        Student student = student();
        Feedback first = new Feedback(1, student, "First", "First content", LocalDateTime.now().minusDays(1));
        Feedback second = new Feedback(2, student, "Second", "Second content", LocalDateTime.now());
        feedbackPort.saved.add(first);
        feedbackPort.saved.add(second);
        responsePort.saved.add(new FeedbackResponse(1, first, admin(), "We will review this.", LocalDateTime.now().minusHours(5)));

        StudentFeedbackService service = new StudentFeedbackService(
                studentId -> Optional.of(student),
                feedbackPort,
                responsePort,
                userId -> Optional.of(admin()),
                feedbackPort,
                responsePort,
                new RecordingTranslationTaskPort()
        );

        List<StudentFeedbackResult> result = service.getStudentFeedback(
                new GetStudentFeedbackQuery(0, 10, "createdAt", "desc"),
                student.getId()
        ).items();

        assertEquals(2, result.size());
        assertEquals("Second", result.get(0).title());
        assertEquals("First", result.get(1).title());
        assertEquals(1, result.get(1).responses().size());
        assertEquals("We will review this.", result.get(1).responses().get(0).content());
    }

    @Test
    void shouldAllowAdminToRespondToFeedback() {
        RecordingFeedbackPort feedbackPort = new RecordingFeedbackPort();
        RecordingFeedbackResponsePort responsePort = new RecordingFeedbackResponsePort();
        Student student = student();
        feedbackPort.saved.add(new Feedback(1, student, "Navigation", "Improve filters.", LocalDateTime.now()));

        StudentFeedbackService service = new StudentFeedbackService(
                studentId -> Optional.of(student),
                feedbackPort,
                responsePort,
                userId -> Optional.of(admin()),
                feedbackPort,
                responsePort,
                new RecordingTranslationTaskPort()
        );

        RespondToFeedbackResult result = service.respond(
                new RespondToFeedbackCommand(1, admin().getId(), "Thanks, we will update it.")
        );

        assertTrue(result.success());
        assertEquals("FEEDBACK_RESPONSE_CREATED", result.code());
        assertEquals(1, responsePort.saved.size());
    }

    @Test
    void shouldRejectStudentResponder() {
        RecordingFeedbackPort feedbackPort = new RecordingFeedbackPort();
        RecordingFeedbackResponsePort responsePort = new RecordingFeedbackResponsePort();
        Student student = student();
        feedbackPort.saved.add(new Feedback(1, student, "Navigation", "Improve filters.", LocalDateTime.now()));

        StudentFeedbackService service = new StudentFeedbackService(
                studentId -> Optional.of(student),
                feedbackPort,
                responsePort,
                userId -> Optional.of(student.getUser()),
                feedbackPort,
                responsePort,
                new RecordingTranslationTaskPort()
        );

        RespondToFeedbackResult result = service.respond(
                new RespondToFeedbackCommand(1, student.getUser().getId(), "Student cannot answer.")
        );

        assertFalse(result.success());
        assertEquals("FORBIDDEN", result.code());
    }

    private Student student() {
        return new Student(
                3,
                new User(3, "student.active@university.edu", "secret", Role.STUDENT, true),
                "Active Student",
                "S0001",
                new Department(1, "Computer Science"),
                Status.ACTIVE,
                null,
                null,
                null,
                null,
                null,
                null,
                0
        );
    }

    private User admin() {
        return new User(1, "admin@university.edu", "secret", Role.ADMIN, true);
    }

    private static final class RecordingFeedbackPort implements LoadFeedbackPort, SaveFeedbackPort {
        private final List<Feedback> saved = new ArrayList<>();

        @Override
        public List<Feedback> loadByStudentId(Integer studentId) {
            return saved.stream()
                    .filter(item -> item.getStudent().getId().equals(studentId))
                    .sorted(Comparator.comparing(Feedback::getCreatedAt).reversed())
                    .toList();
        }

        @Override
        public StudentFeedbackSearchPage loadStudentPage(com.ttcs.backend.application.port.out.LoadStudentFeedbackQuery query) {
            List<StudentFeedbackSearchItem> items = saved.stream()
                    .filter(item -> item.getStudent().getId().equals(query.studentId()))
                    .sorted(Comparator.comparing(Feedback::getCreatedAt).reversed())
                    .map(item -> new StudentFeedbackSearchItem(
                            item.getId(),
                            item.getTitle(),
                            item.getContent(),
                            item.getContentOriginal(),
                            item.getContentTranslated(),
                            item.getSourceLang(),
                            item.isAutoTranslated(),
                            item.getCreatedAt()
                    ))
                    .toList();
            return new StudentFeedbackSearchPage(items, query.page(), query.size(), items.size(), items.isEmpty() ? 0 : 1);
        }

        @Override
        public StaffFeedbackSearchPage loadPage(LoadFeedbackQuery query) {
            return new StaffFeedbackSearchPage(List.of(), query.page(), query.size(), 0, 0);
        }

        @Override
        public Optional<Feedback> loadById(Integer feedbackId) {
            return saved.stream().filter(item -> item.getId().equals(feedbackId)).findFirst();
        }

        @Override
        public Feedback save(Feedback feedback) {
            Feedback savedFeedback = new Feedback(
                    saved.size() + 1,
                    feedback.getStudent(),
                    feedback.getTitle(),
                    feedback.getContent(),
                    feedback.getContentOriginal(),
                    feedback.getContentTranslated(),
                    feedback.getSourceLang(),
                    feedback.isAutoTranslated(),
                    feedback.getCreatedAt()
            );
            saved.add(savedFeedback);
            return savedFeedback;
        }
    }

    private static final class RecordingTranslationTaskPort implements SendTranslationTaskPort {
        private final List<TranslationTaskCommand> sent = new ArrayList<>();

        @Override
        public void send(TranslationTaskCommand command) {
            sent.add(command);
        }
    }

    private static final class RecordingFeedbackResponsePort implements LoadFeedbackResponsePort, SaveFeedbackResponsePort {
        private final List<FeedbackResponse> saved = new ArrayList<>();

        @Override
        public List<FeedbackResponse> loadByFeedbackIds(List<Integer> feedbackIds) {
            return saved.stream()
                    .filter(item -> feedbackIds.contains(item.getFeedback().getId()))
                    .sorted(Comparator.comparing(FeedbackResponse::getCreatedAt))
                    .toList();
        }

        @Override
        public List<FeedbackResponse> loadByFeedbackId(Integer feedbackId) {
            return saved.stream()
                    .filter(item -> item.getFeedback().getId().equals(feedbackId))
                    .sorted(Comparator.comparing(FeedbackResponse::getCreatedAt))
                    .toList();
        }

        @Override
        public FeedbackResponse save(FeedbackResponse response) {
            FeedbackResponse savedResponse = new FeedbackResponse(
                    saved.size() + 1,
                    response.getFeedback(),
                    response.getResponder(),
                    response.getContent(),
                    response.getCreatedAt()
            );
            saved.add(savedResponse);
            return savedResponse;
        }
    }
}
