package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.model.Feedback;
import com.ttcs.backend.application.domain.model.FeedbackResponse;
import com.ttcs.backend.application.domain.model.Role;
import com.ttcs.backend.application.domain.model.Student;
import com.ttcs.backend.application.domain.model.User;
import com.ttcs.backend.application.port.in.feedback.GetAllFeedbackUseCase;
import com.ttcs.backend.application.port.in.feedback.CreateFeedbackUseCase;
import com.ttcs.backend.application.port.in.feedback.GetStudentFeedbackUseCase;
import com.ttcs.backend.application.port.in.feedback.RespondToFeedbackUseCase;
import com.ttcs.backend.application.port.in.feedback.command.CreateFeedbackCommand;
import com.ttcs.backend.application.port.in.feedback.command.RespondToFeedbackCommand;
import com.ttcs.backend.application.port.in.feedback.result.CreateFeedbackResult;
import com.ttcs.backend.application.port.in.feedback.result.FeedbackResponseResult;
import com.ttcs.backend.application.port.in.feedback.result.RespondToFeedbackResult;
import com.ttcs.backend.application.port.in.feedback.result.StaffFeedbackResult;
import com.ttcs.backend.application.port.in.feedback.result.StudentFeedbackResult;
import com.ttcs.backend.application.port.out.LoadFeedbackPort;
import com.ttcs.backend.application.port.out.LoadFeedbackResponsePort;
import com.ttcs.backend.application.port.out.LoadStudentPort;
import com.ttcs.backend.application.port.out.SaveFeedbackPort;
import com.ttcs.backend.application.port.out.SaveFeedbackResponsePort;
import com.ttcs.backend.application.port.out.auth.LoadUserByIdPort;
import com.ttcs.backend.common.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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

        Feedback feedback = new Feedback(
                null,
                student,
                command.title().trim(),
                command.content().trim(),
                LocalDateTime.now()
        );
        saveFeedbackPort.save(feedback);
        return CreateFeedbackResult.ok();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentFeedbackResult> getStudentFeedback(Integer studentId) {
        List<Feedback> feedback = loadFeedbackPort.loadByStudentId(studentId);
        Map<Integer, List<FeedbackResponseResult>> responsesByFeedbackId = mapResponses(feedback);

        return feedback.stream()
                .map(item -> new StudentFeedbackResult(
                        item.getId(),
                        item.getTitle(),
                        item.getContent(),
                        item.getCreatedAt(),
                        responsesByFeedbackId.getOrDefault(item.getId(), List.of())
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffFeedbackResult> getAllFeedback() {
        List<Feedback> feedback = loadFeedbackPort.loadAll();
        Map<Integer, List<FeedbackResponseResult>> responsesByFeedbackId = mapResponses(feedback);

        return feedback.stream()
                .sorted(Comparator.comparing(Feedback::getCreatedAt).reversed())
                .map(item -> new StaffFeedbackResult(
                        item.getId(),
                        item.getStudent().getId(),
                        item.getStudent().getName(),
                        item.getStudent().getUser() != null ? item.getStudent().getUser().getEmail() : null,
                        item.getTitle(),
                        item.getContent(),
                        item.getCreatedAt(),
                        responsesByFeedbackId.getOrDefault(item.getId(), List.of())
                ))
                .toList();
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
        if (responder.getRole() != Role.ADMIN && responder.getRole() != Role.TEACHER) {
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

    private Map<Integer, List<FeedbackResponseResult>> mapResponses(List<Feedback> feedback) {
        if (feedback.isEmpty()) {
            return Map.of();
        }

        List<Integer> ids = feedback.stream().map(Feedback::getId).toList();
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
