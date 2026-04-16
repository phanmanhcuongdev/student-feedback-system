package com.ttcs.backend.adapter.in.web;

import com.ttcs.backend.adapter.in.web.dto.CreateSurveyRequest;
import com.ttcs.backend.adapter.in.web.dto.CreateSurveyResponse;
import com.ttcs.backend.adapter.in.web.dto.DepartmentOptionResponse;
import com.ttcs.backend.adapter.in.web.dto.ManagedSurveyMetricsResponse;
import com.ttcs.backend.adapter.in.web.dto.ManagedSurveyPageResponse;
import com.ttcs.backend.adapter.in.web.dto.SetSurveyVisibilityRequest;
import com.ttcs.backend.adapter.in.web.dto.SurveyManagementActionResponse;
import com.ttcs.backend.adapter.in.web.dto.SurveyManagementDetailResponse;
import com.ttcs.backend.adapter.in.web.dto.SurveyManagementQuestionResponse;
import com.ttcs.backend.adapter.in.web.dto.SurveyManagementRecipientResponse;
import com.ttcs.backend.adapter.in.web.dto.SurveyManagementSummaryResponse;
import com.ttcs.backend.adapter.in.web.dto.UpdateSurveyRequest;
import com.ttcs.backend.application.domain.model.QuestionType;
import com.ttcs.backend.application.domain.model.SurveyRecipientScope;
import com.ttcs.backend.application.port.in.CreateSurveyUseCase;
import com.ttcs.backend.application.port.in.admin.ArchiveSurveyUseCase;
import com.ttcs.backend.application.port.in.admin.CloseSurveyUseCase;
import com.ttcs.backend.application.port.in.admin.GetManagedSurveysQuery;
import com.ttcs.backend.application.port.in.admin.GetManagedSurveyDetailUseCase;
import com.ttcs.backend.application.port.in.admin.GetManagedSurveysUseCase;
import com.ttcs.backend.application.port.in.admin.GetSurveyManagementDepartmentsUseCase;
import com.ttcs.backend.application.port.in.admin.ManagedSurveyPageResult;
import com.ttcs.backend.application.port.in.admin.PublishSurveyUseCase;
import com.ttcs.backend.application.port.in.admin.SetSurveyHiddenUseCase;
import com.ttcs.backend.application.port.in.admin.SurveyManagementActionResult;
import com.ttcs.backend.application.port.in.admin.SurveyManagementDetailResult;
import com.ttcs.backend.application.port.in.admin.SurveyManagementSummaryResult;
import com.ttcs.backend.application.port.in.admin.UpdateSurveyCommand;
import com.ttcs.backend.application.port.in.admin.UpdateSurveyQuestionCommand;
import com.ttcs.backend.application.port.in.admin.UpdateSurveyUseCase;
import com.ttcs.backend.application.port.in.command.CreateQuestionCommand;
import com.ttcs.backend.application.port.in.command.CreateSurveyCommand;
import com.ttcs.backend.common.WebAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@WebAdapter
@RestController
@RequestMapping("/api/admin/surveys")
@RequiredArgsConstructor
public class AdminSurveyController {

    private final CreateSurveyUseCase createSurveyUseCase;
    private final GetManagedSurveysUseCase getManagedSurveysUseCase;
    private final GetManagedSurveyDetailUseCase getManagedSurveyDetailUseCase;
    private final GetSurveyManagementDepartmentsUseCase getSurveyManagementDepartmentsUseCase;
    private final UpdateSurveyUseCase updateSurveyUseCase;
    private final SetSurveyHiddenUseCase setSurveyHiddenUseCase;
    private final CloseSurveyUseCase closeSurveyUseCase;
    private final PublishSurveyUseCase publishSurveyUseCase;
    private final ArchiveSurveyUseCase archiveSurveyUseCase;
    private final CurrentStudentProvider currentStudentProvider;

    @GetMapping
    public ResponseEntity<ManagedSurveyPageResponse> getSurveys(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String lifecycleState,
            @RequestParam(required = false) String runtimeStatus,
            @RequestParam(required = false) Boolean hidden,
            @RequestParam(required = false) String recipientScope,
            @RequestParam(required = false) LocalDate startDateFrom,
            @RequestParam(required = false) LocalDate endDateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "startDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        ManagedSurveyPageResult result = getManagedSurveysUseCase.getSurveys(new GetManagedSurveysQuery(
                keyword,
                lifecycleState,
                runtimeStatus,
                hidden,
                recipientScope,
                startDateFrom,
                endDateTo,
                page,
                size,
                sortBy,
                sortDir
        ));

        return ResponseEntity.ok(new ManagedSurveyPageResponse(
                result.items().stream().map(this::toSummaryResponse).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                new ManagedSurveyMetricsResponse(
                        result.metrics().totalSurveys(),
                        result.metrics().totalDrafts(),
                        result.metrics().totalPublished(),
                        result.metrics().totalOpen(),
                        result.metrics().totalClosed(),
                        result.metrics().totalHidden()
                )
        ));
    }

    @GetMapping("/departments")
    public ResponseEntity<List<DepartmentOptionResponse>> getDepartments() {
        return ResponseEntity.ok(getSurveyManagementDepartmentsUseCase.getDepartments().stream()
                .map(department -> new DepartmentOptionResponse(department.getId(), department.getName()))
                .toList());
    }

    @GetMapping("/{surveyId}")
    public ResponseEntity<SurveyManagementDetailResponse> getSurvey(@PathVariable Integer surveyId) {
        return ResponseEntity.ok(toDetailResponse(getManagedSurveyDetailUseCase.getSurvey(surveyId)));
    }

    @PostMapping
    public ResponseEntity<CreateSurveyResponse> createSurvey(@RequestBody CreateSurveyRequest request) {
        Integer adminId = currentStudentProvider.currentUserId();

        List<CreateQuestionCommand> questionCommands = request.questions() == null ? List.of() :
                request.questions().stream()
                        .map(q -> new CreateQuestionCommand(q.content(), QuestionType.valueOf(q.type())))
                        .toList();

        CreateSurveyCommand command = new CreateSurveyCommand(
                request.title(),
                request.description(),
                request.startDate(),
                request.endDate(),
                adminId,
                questionCommands,
                parseScope(request.recipientScope()),
                request.recipientDepartmentId()
        );

        Integer surveyId = createSurveyUseCase.createSurvey(command);

        return ResponseEntity.ok(new CreateSurveyResponse(
                true,
                surveyId,
                "SURVEY_CREATED",
                "Survey draft created successfully."
        ));
    }

    @PutMapping("/{surveyId}")
    public ResponseEntity<SurveyManagementActionResponse> updateSurvey(
            @PathVariable Integer surveyId,
            @RequestBody UpdateSurveyRequest request
    ) {
        List<UpdateSurveyQuestionCommand> questions = request.questions() == null ? List.of() :
                request.questions().stream()
                        .map(q -> new UpdateSurveyQuestionCommand(q.content(), q.type()))
                        .toList();

        SurveyManagementActionResult result = updateSurveyUseCase.updateSurvey(
                new UpdateSurveyCommand(
                        surveyId,
                        request.title(),
                        request.description(),
                        request.startDate(),
                        request.endDate(),
                        questions,
                        parseScope(request.recipientScope()),
                        request.recipientDepartmentId()
                )
        );
        return ResponseEntity.ok(toActionResponse(result));
    }

    @PostMapping("/{surveyId}/publish")
    public ResponseEntity<SurveyManagementActionResponse> publishSurvey(@PathVariable Integer surveyId) {
        return ResponseEntity.ok(toActionResponse(publishSurveyUseCase.publishSurvey(surveyId, currentStudentProvider.currentUserId())));
    }

    @PostMapping("/{surveyId}/close")
    public ResponseEntity<SurveyManagementActionResponse> closeSurvey(@PathVariable Integer surveyId) {
        return ResponseEntity.ok(toActionResponse(closeSurveyUseCase.closeSurvey(surveyId, currentStudentProvider.currentUserId())));
    }

    @PostMapping("/{surveyId}/archive")
    public ResponseEntity<SurveyManagementActionResponse> archiveSurvey(@PathVariable Integer surveyId) {
        return ResponseEntity.ok(toActionResponse(archiveSurveyUseCase.archiveSurvey(surveyId, currentStudentProvider.currentUserId())));
    }

    @PostMapping("/{surveyId}/visibility")
    public ResponseEntity<SurveyManagementActionResponse> setVisibility(
            @PathVariable Integer surveyId,
            @RequestBody SetSurveyVisibilityRequest request
    ) {
        return ResponseEntity.ok(toActionResponse(setSurveyHiddenUseCase.setHidden(surveyId, request.hidden(), currentStudentProvider.currentUserId())));
    }

    private SurveyRecipientScope parseScope(String rawScope) {
        if (rawScope == null || rawScope.isBlank()) {
            return SurveyRecipientScope.ALL_STUDENTS;
        }
        return SurveyRecipientScope.valueOf(rawScope);
    }

    private SurveyManagementSummaryResponse toSummaryResponse(SurveyManagementSummaryResult result) {
        return new SurveyManagementSummaryResponse(
                result.id(),
                result.title(),
                result.description(),
                result.startDate(),
                result.endDate(),
                result.lifecycleState(),
                result.runtimeStatus(),
                result.hidden(),
                result.recipientScope(),
                result.recipientDepartmentId(),
                result.recipientDepartmentName(),
                result.responseCount(),
                result.targetedCount(),
                result.openedCount(),
                result.submittedCount(),
                result.responseRate()
        );
    }

    private SurveyManagementDetailResponse toDetailResponse(SurveyManagementDetailResult result) {
        return new SurveyManagementDetailResponse(
                result.id(),
                result.title(),
                result.description(),
                result.startDate(),
                result.endDate(),
                result.lifecycleState(),
                result.runtimeStatus(),
                result.hidden(),
                result.recipientScope(),
                result.recipientDepartmentId(),
                result.recipientDepartmentName(),
                result.responseCount(),
                result.targetedCount(),
                result.openedCount(),
                result.submittedCount(),
                result.responseRate(),
                result.questions().stream()
                        .map(item -> new SurveyManagementQuestionResponse(item.id(), item.content(), item.type()))
                        .toList(),
                result.pendingRecipients().stream()
                        .map(item -> new SurveyManagementRecipientResponse(
                                item.studentId(),
                                item.studentName(),
                                item.studentCode(),
                                item.departmentName(),
                                item.participationStatus(),
                                item.openedAt(),
                                item.submittedAt()
                        ))
                        .toList()
        );
    }

    private SurveyManagementActionResponse toActionResponse(SurveyManagementActionResult result) {
        return new SurveyManagementActionResponse(result.success(), result.code(), result.message());
    }
}
