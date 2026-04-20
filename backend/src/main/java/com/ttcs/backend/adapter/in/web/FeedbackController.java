package com.ttcs.backend.adapter.in.web;

import com.ttcs.backend.adapter.in.web.dto.CreateFeedbackRequest;
import com.ttcs.backend.adapter.in.web.dto.CreateFeedbackResponse;
import com.ttcs.backend.adapter.in.web.dto.FeedbackResponseView;
import com.ttcs.backend.adapter.in.web.dto.RespondToFeedbackRequest;
import com.ttcs.backend.adapter.in.web.dto.RespondToFeedbackResponse;
import com.ttcs.backend.adapter.in.web.dto.StaffFeedbackPageResponse;
import com.ttcs.backend.adapter.in.web.dto.StaffFeedbackResponse;
import com.ttcs.backend.adapter.in.web.dto.StudentFeedbackPageResponse;
import com.ttcs.backend.adapter.in.web.dto.StudentFeedbackResponse;
import com.ttcs.backend.application.port.in.feedback.CreateFeedbackUseCase;
import com.ttcs.backend.application.port.in.feedback.GetAllFeedbackQuery;
import com.ttcs.backend.application.port.in.feedback.GetAllFeedbackUseCase;
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
import com.ttcs.backend.common.WebAdapter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@WebAdapter
@RequestMapping("/api/v1/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final CreateFeedbackUseCase createFeedbackUseCase;
    private final GetStudentFeedbackUseCase getStudentFeedbackUseCase;
    private final GetAllFeedbackUseCase getAllFeedbackUseCase;
    private final RespondToFeedbackUseCase respondToFeedbackUseCase;
    private final CurrentIdentityProvider currentIdentityProvider;

    @GetMapping
    public ResponseEntity<StudentFeedbackPageResponse> getFeedback(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        currentIdentityProvider.ensureActiveStudentAccount();
        Integer studentId = currentIdentityProvider.currentStudentProfileId();
        StudentFeedbackPageResult result = getStudentFeedbackUseCase.getStudentFeedback(
                new GetStudentFeedbackQuery(page, size, sortBy, sortDir),
                studentId
        );
        return ResponseEntity.ok(new StudentFeedbackPageResponse(
                result.items().stream().map(this::toResponse).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages()
        ));
    }

    @GetMapping("/staff")
    public ResponseEntity<StaffFeedbackPageResponse> getAllFeedback(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDate createdDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        StaffFeedbackPageResult result = getAllFeedbackUseCase.getAllFeedback(new GetAllFeedbackQuery(
                keyword,
                status,
                createdDate,
                page,
                size,
                sortBy,
                sortDir
        ));
        return ResponseEntity.ok(new StaffFeedbackPageResponse(
                result.items().stream().map(this::toStaffResponse).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages()
        ));
    }

    @PostMapping
    public ResponseEntity<CreateFeedbackResponse> createFeedback(@Valid @RequestBody CreateFeedbackRequest request) {
        currentIdentityProvider.ensureActiveStudentAccount();
        CreateFeedbackResult result = createFeedbackUseCase.createFeedback(
                new CreateFeedbackCommand(
                        currentIdentityProvider.currentStudentProfileId(),
                        request.getTitle(),
                        request.getContent()
                )
        );
        return ResponseEntity.ok(new CreateFeedbackResponse(result.success(), result.code(), result.message()));
    }

    @PostMapping("/{feedbackId}/responses")
    public ResponseEntity<RespondToFeedbackResponse> respondToFeedback(
            @PathVariable Integer feedbackId,
            @Valid @RequestBody RespondToFeedbackRequest request
    ) {
        RespondToFeedbackResult result = respondToFeedbackUseCase.respond(
                new RespondToFeedbackCommand(
                        feedbackId,
                        currentIdentityProvider.currentUserId(),
                        request.getContent()
                )
        );
        return ResponseEntity.ok(new RespondToFeedbackResponse(result.success(), result.code(), result.message()));
    }

    private StudentFeedbackResponse toResponse(StudentFeedbackResult result) {
        return new StudentFeedbackResponse(
                result.id(),
                result.title(),
                result.content(),
                result.createdAt(),
                result.responses().stream().map(this::toResponseView).toList()
        );
    }

    private StaffFeedbackResponse toStaffResponse(StaffFeedbackResult result) {
        return new StaffFeedbackResponse(
                result.id(),
                result.studentId(),
                result.studentName(),
                result.studentEmail(),
                result.title(),
                result.content(),
                result.createdAt(),
                result.responses().stream().map(this::toResponseView).toList()
        );
    }

    private FeedbackResponseView toResponseView(FeedbackResponseResult result) {
        return new FeedbackResponseView(
                result.id(),
                result.responderEmail(),
                result.responderRole(),
                result.content(),
                result.createdAt()
        );
    }
}
