package com.ttcs.backend.adapter.in.web;

import com.ttcs.backend.adapter.in.web.dto.CreateSurveyRequest;
import com.ttcs.backend.adapter.in.web.dto.SurveyManagementActionResponse;
import com.ttcs.backend.adapter.in.web.dto.SurveyManagementDetailResponse;
import com.ttcs.backend.adapter.in.web.dto.SurveyManagementQuestionResponse;
import com.ttcs.backend.adapter.in.web.dto.SurveyManagementSummaryResponse;
import com.ttcs.backend.adapter.in.web.dto.SetSurveyVisibilityRequest;
import com.ttcs.backend.adapter.in.web.dto.UpdateSurveyRequest;
import com.ttcs.backend.application.domain.model.QuestionType;
import com.ttcs.backend.application.domain.model.SurveyRecipientScope;
import com.ttcs.backend.application.port.in.CreateSurveyUseCase;
import com.ttcs.backend.application.port.in.command.CreateQuestionCommand;
import com.ttcs.backend.application.port.in.command.CreateSurveyCommand;
import com.ttcs.backend.application.port.in.admin.CloseSurveyUseCase;
import com.ttcs.backend.application.port.in.admin.GetManagedSurveyDetailUseCase;
import com.ttcs.backend.application.port.in.admin.GetManagedSurveysUseCase;
import com.ttcs.backend.application.port.in.admin.SurveyManagementActionResult;
import com.ttcs.backend.application.port.in.admin.SurveyManagementDetailResult;
import com.ttcs.backend.application.port.in.admin.SurveyManagementSummaryResult;
import com.ttcs.backend.application.port.in.admin.SetSurveyHiddenUseCase;
import com.ttcs.backend.application.port.in.admin.UpdateSurveyCommand;
import com.ttcs.backend.application.port.in.admin.UpdateSurveyQuestionCommand;
import com.ttcs.backend.application.port.in.admin.UpdateSurveyUseCase;
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

import java.util.List;
import java.util.Map;

@WebAdapter
@RestController
@RequestMapping("/api/admin/surveys")
@RequiredArgsConstructor
public class AdminSurveyController {

    private final CreateSurveyUseCase createSurveyUseCase;
    private final GetManagedSurveysUseCase getManagedSurveysUseCase;
    private final GetManagedSurveyDetailUseCase getManagedSurveyDetailUseCase;
    private final UpdateSurveyUseCase updateSurveyUseCase;
    private final SetSurveyHiddenUseCase setSurveyHiddenUseCase;
    private final CloseSurveyUseCase closeSurveyUseCase;
    private final CurrentStudentProvider currentStudentProvider;

    @GetMapping
    public ResponseEntity<List<SurveyManagementSummaryResponse>> getSurveys() {
        return ResponseEntity.ok(getManagedSurveysUseCase.getSurveys().stream().map(this::toSummaryResponse).toList());
    }

    @GetMapping("/{surveyId}")
    public ResponseEntity<SurveyManagementDetailResponse> getSurvey(@PathVariable Integer surveyId) {
        return ResponseEntity.ok(toDetailResponse(getManagedSurveyDetailUseCase.getSurvey(surveyId)));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createSurvey(@RequestBody CreateSurveyRequest request) {
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

        return ResponseEntity.ok(Map.of(
                "success", true,
                "surveyId", surveyId,
                "message", "Survey created successfully"
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

    @PostMapping("/{surveyId}/close")
    public ResponseEntity<SurveyManagementActionResponse> closeSurvey(@PathVariable Integer surveyId) {
        return ResponseEntity.ok(toActionResponse(closeSurveyUseCase.closeSurvey(surveyId)));
    }

    @PostMapping("/{surveyId}/visibility")
    public ResponseEntity<SurveyManagementActionResponse> setVisibility(
            @PathVariable Integer surveyId,
            @RequestBody SetSurveyVisibilityRequest request
    ) {
        return ResponseEntity.ok(toActionResponse(setSurveyHiddenUseCase.setHidden(surveyId, request.hidden())));
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
                result.status(),
                result.hidden(),
                result.recipientScope(),
                result.recipientDepartmentId(),
                result.responseCount()
        );
    }

    private SurveyManagementDetailResponse toDetailResponse(SurveyManagementDetailResult result) {
        return new SurveyManagementDetailResponse(
                result.id(),
                result.title(),
                result.description(),
                result.startDate(),
                result.endDate(),
                result.status(),
                result.hidden(),
                result.recipientScope(),
                result.recipientDepartmentId(),
                result.responseCount(),
                result.questions().stream()
                        .map(item -> new SurveyManagementQuestionResponse(item.id(), item.content(), item.type()))
                        .toList()
        );
    }

    private SurveyManagementActionResponse toActionResponse(SurveyManagementActionResult result) {
        return new SurveyManagementActionResponse(result.success(), result.code(), result.message());
    }
}
