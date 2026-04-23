package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.model.Feedback;
import com.ttcs.backend.application.domain.model.FeedbackResponse;
import com.ttcs.backend.application.domain.model.Role;
import com.ttcs.backend.application.domain.model.Student;
import com.ttcs.backend.application.domain.model.User;
import com.ttcs.backend.application.port.in.feedback.GetAllFeedbackUseCase;
import com.ttcs.backend.application.port.in.feedback.CreateFeedbackUseCase;
import com.ttcs.backend.application.port.in.feedback.GetAllFeedbackQuery;
import com.ttcs.backend.application.port.in.feedback.GetStudentFeedbackQuery;
import com.ttcs.backend.application.port.in.feedback.GetStudentFeedbackUseCase;
import com.ttcs.backend.application.port.in.feedback.RespondToFeedbackUseCase;
import com.ttcs.backend.application.port.in.feedback.StaffFeedbackPageResult;
import com.ttcs.backend.application.port.in.feedback.StudentFeedbackPageResult;
import com.ttcs.backend.application.port.in.feedback.command.CreateFeedbackCommand;
import com.ttcs.backend.application.port.in.feedback.command.RespondToFeedbackCommand;
import com.ttcs.backend.application.port.in.feedback.result.CreateFeedbackResult;
import com.ttcs.backend.application.port.in.feedback.result.FeedbackResponseResult;
import com.ttcs.backend.application.port.in.feedback.result.RespondToFeedbackResult;
import com.ttcs.backend.application.port.in.feedback.result.StaffFeedbackResult;
import com.ttcs.backend.application.port.in.feedback.result.StudentFeedbackResult;
import com.ttcs.backend.application.port.out.LoadFeedbackPort;
import com.ttcs.backend.application.port.out.LoadFeedbackQuery;
import com.ttcs.backend.application.port.out.LoadFeedbackResponsePort;
import com.ttcs.backend.application.port.out.LoadStudentFeedbackQuery;
import com.ttcs.backend.application.port.out.LoadStudentPort;
import com.ttcs.backend.application.port.out.StaffFeedbackSearchItem;
import com.ttcs.backend.application.port.out.StudentFeedbackSearchItem;
import com.ttcs.backend.application.port.out.SaveFeedbackPort;
import com.ttcs.backend.application.port.out.SaveFeedbackResponsePort;
import com.ttcs.backend.application.port.out.ai.SendTranslationTaskPort;
import com.ttcs.backend.application.port.out.ai.TranslationTaskCommand;
import com.ttcs.backend.application.port.out.auth.LoadUserByIdPort;
import com.ttcs.backend.common.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@UseCase
@RequiredArgsConstructor
public class StudentFeedbackService implements
        CreateFeedbackUseCase,
        GetStudentFeedbackUseCase,
        GetAllFeedbackUseCase,
        RespondToFeedbackUseCase {

    private final LoadStudentPort loadStudentPort;
    private final LoadFeedbackPort loadFeedbackPort;
    private final LoadFeedbackResponsePort loadFeedbackResponsePort;
    private final LoadUserByIdPort loadUserByIdPort;
    private final SaveFeedbackPort saveFeedbackPort;
    private final SaveFeedbackResponsePort saveFeedbackResponsePort;
    private final SendTranslationTaskPort sendTranslationTaskPort;

    @Override
    @Transactional
    public CreateFeedbackResult createFeedback(CreateFeedbackCommand command) {
        if (command == null
                || command.studentId() == null
                || isBlank(command.title())
                || isBlank(command.content())) {
            return CreateFeedbackResult.fail("INVALID_INPUT", "Title and content are required.");
        }

        Student student = loadStudentPort.loadById(command.studentId()).orElse(null);
        if (student == null) {
            return CreateFeedbackResult.fail("STUDENT_NOT_FOUND", "Student profile not found.");
        }

        String contentOriginal = command.content();
        String content = contentOriginal.trim();
        Feedback feedback = new Feedback(
                null,
                student,
                command.title().trim(),
                content,
                contentOriginal,
                null,
                null,
                null,
                null,
                false,
                LocalDateTime.now()
        );
        Feedback savedFeedback = saveFeedbackPort.save(feedback);
        sendTranslationTaskAfterCommit(new TranslationTaskCommand(
                savedFeedback.getId(),
                "FEEDBACK",
                savedFeedback.getContentOriginal(),
                normalizeLanguage(command.targetLang())
        ));
        return CreateFeedbackResult.ok();
    }

    @Override
    @Transactional(readOnly = true)
    public StudentFeedbackPageResult getStudentFeedback(GetStudentFeedbackQuery query, Integer studentId) {
        var page = loadFeedbackPort.loadStudentPage(new LoadStudentFeedbackQuery(
                studentId,
                query == null ? 0 : query.page(),
                query == null ? 5 : query.size(),
                query == null ? "createdAt" : query.sortBy(),
                query == null ? "desc" : query.sortDir()
        ));
        Map<Integer, List<FeedbackResponseResult>> responsesByFeedbackId = mapResponsesByFeedbackIds(
                page.items().stream().map(StudentFeedbackSearchItem::id).toList()
        );

        return new StudentFeedbackPageResult(
                page.items().stream()
                        .map(item -> new StudentFeedbackResult(
                                item.id(),
                                item.title(),
                                item.content(),
                                item.contentOriginal(),
                                item.contentVi(),
                                item.contentEn(),
                                item.sourceLang(),
                                item.isAutoTranslated(),
                                item.createdAt(),
                                responsesByFeedbackId.getOrDefault(item.id(), List.of())
                        ))
                        .toList(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public StaffFeedbackPageResult getAllFeedback(GetAllFeedbackQuery query) {
        var page = loadFeedbackPort.loadPage(new LoadFeedbackQuery(
                query == null ? null : query.keyword(),
                query == null ? null : query.status(),
                query == null ? null : query.createdDate(),
                query == null ? 0 : query.page(),
                query == null ? 10 : query.size(),
                query == null ? "createdAt" : query.sortBy(),
                query == null ? "desc" : query.sortDir()
        ));
        Map<Integer, List<FeedbackResponseResult>> responsesByFeedbackId = mapResponsesByFeedbackIds(
                page.items().stream().map(StaffFeedbackSearchItem::id).toList()
        );

        return new StaffFeedbackPageResult(
                page.items().stream()
                        .map(item -> new StaffFeedbackResult(
                                item.id(),
                                item.studentId(),
                                item.studentName(),
                                item.studentEmail(),
                                item.title(),
                                item.content(),
                                item.contentOriginal(),
                                item.contentVi(),
                                item.contentEn(),
                                item.sourceLang(),
                                item.isAutoTranslated(),
                                item.createdAt(),
                                responsesByFeedbackId.getOrDefault(item.id(), List.of())
                        ))
                        .toList(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages()
        );
    }

    @Override
    @Transactional
    public RespondToFeedbackResult respond(RespondToFeedbackCommand command) {
        if (command == null
                || command.feedbackId() == null
                || command.responderUserId() == null
                || isBlank(command.content())) {
            return RespondToFeedbackResult.fail("INVALID_INPUT", "Response content is required.");
        }

        Feedback feedback = loadFeedbackPort.loadById(command.feedbackId()).orElse(null);
        if (feedback == null) {
            return RespondToFeedbackResult.fail("FEEDBACK_NOT_FOUND", "Feedback item not found.");
        }

        User responder = loadUserByIdPort.loadById(command.responderUserId()).orElse(null);
        if (responder == null) {
            return RespondToFeedbackResult.fail("RESPONDER_NOT_FOUND", "Responder account not found.");
        }
        if (responder.getRole() != Role.ADMIN && responder.getRole() != Role.LECTURER) {
            return RespondToFeedbackResult.fail("FORBIDDEN", "Only admin or lecturer accounts can respond to feedback.");
        }

        FeedbackResponse response = new FeedbackResponse(
                null,
                feedback,
                responder,
                command.content().trim(),
                LocalDateTime.now()
        );
        saveFeedbackResponsePort.save(response);
        return RespondToFeedbackResult.ok();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalizeLanguage(String value) {
        if (isBlank(value)) {
            return "vi";
        }
        String language = value.split(",")[0].trim().split("-")[0].toLowerCase();
        return "en".equals(language) ? "en" : "vi";
    }

    private void sendTranslationTaskAfterCommit(TranslationTaskCommand command) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            sendTranslationTaskPort.send(command);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                sendTranslationTaskPort.send(command);
            }
        });
    }

    private Map<Integer, List<FeedbackResponseResult>> mapResponsesByFeedbackIds(List<Integer> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return loadFeedbackResponsePort.loadByFeedbackIds(ids).stream()
                .map(this::toResponseResult)
                .collect(Collectors.groupingBy(FeedbackResponseResult::feedbackId));
    }

    private FeedbackResponseResult toResponseResult(FeedbackResponse response) {
        return new FeedbackResponseResult(
                response.getId(),
                response.getFeedback().getId(),
                response.getResponder().getId(),
                response.getResponder().getEmail(),
                response.getResponder().getRole().name(),
                response.getContent(),
                response.getCreatedAt()
        );
    }
}
