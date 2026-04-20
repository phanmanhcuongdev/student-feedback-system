package com.ttcs.backend.adapter.in.web;

import com.ttcs.backend.adapter.in.web.dto.QuestionItemResponse;
import com.ttcs.backend.adapter.in.web.dto.StudentSurveyPageResponse;
import com.ttcs.backend.adapter.in.web.dto.SubmitSurveyRequest;
import com.ttcs.backend.adapter.in.web.dto.SubmitSurveyResponse;
import com.ttcs.backend.adapter.in.web.dto.SubmitSurveyResponseCode;
import com.ttcs.backend.adapter.in.web.dto.SurveyDetailResponse;
import com.ttcs.backend.adapter.in.web.dto.SurveyResponse;
import com.ttcs.backend.application.port.in.GetSurveyDetailUseCase;
import com.ttcs.backend.application.port.in.GetStudentSurveysQuery;
import com.ttcs.backend.application.port.in.GetSurveyUseCase;
import com.ttcs.backend.application.port.in.SubmitSurveyUseCase;
import com.ttcs.backend.application.port.in.command.SubmitSurveyAnswerCommand;
import com.ttcs.backend.application.port.in.command.SubmitSurveyCommand;
import com.ttcs.backend.application.port.in.result.QuestionItemResult;
import com.ttcs.backend.application.port.in.result.StudentSurveyPageResult;
import com.ttcs.backend.application.port.in.result.SubmitSurveyResult;
import com.ttcs.backend.application.port.in.result.SurveyDetailResult;
import com.ttcs.backend.application.port.in.result.SurveySummaryResult;
import com.ttcs.backend.common.WebAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@WebAdapter
@RequestMapping("/api/v1/surveys")
@RequiredArgsConstructor
public class SurveyController {

    private final GetSurveyUseCase getSurveyUseCase;
    private final GetSurveyDetailUseCase getSurveyDetailUseCase;
    private final SubmitSurveyUseCase submitSurveyUseCase;
    private final CurrentIdentityProvider currentIdentityProvider;

    @GetMapping("/{id}")
    public ResponseEntity<SurveyResponse> getSurveyById(@PathVariable("id") Integer surveyId) {
        currentIdentityProvider.ensureActiveStudentAccount();
        return ResponseEntity.ok(toSurveyResponse(getSurveyUseCase.getSurveyById(surveyId, currentIdentityProvider.currentUserId())));
    }

    @GetMapping
    public ResponseEntity<StudentSurveyPageResponse> getAllSurveys(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "endDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        currentIdentityProvider.ensureActiveStudentAccount();
        StudentSurveyPageResult result = getSurveyUseCase.getAllSurveys(
                new GetStudentSurveysQuery(status, page, size, sortBy, sortDir),
                currentIdentityProvider.currentUserId()
        );
        return ResponseEntity.ok(new StudentSurveyPageResponse(
                result.items().stream().map(this::toSurveyResponse).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages()
        ));
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<SurveyDetailResponse> getSurveyDetail(@PathVariable("id") Integer surveyId) {
        currentIdentityProvider.ensureActiveStudentAccount();
        return ResponseEntity.ok(toSurveyDetailResponse(
                getSurveyDetailUseCase.getSurveyDetail(surveyId, currentIdentityProvider.currentStudentProfileId())
        ));
    }

    @PostMapping("/{surveyId}/submit")
    public ResponseEntity<SubmitSurveyResponse> submitSurvey(
            @PathVariable Integer surveyId,
            @RequestBody SubmitSurveyRequest request
    ) {
        currentIdentityProvider.ensureActiveStudentAccount();
        SubmitSurveyCommand command = new SubmitSurveyCommand(
                surveyId,
                currentIdentityProvider.currentStudentProfileId(),
                request == null || request.answers() == null
                        ? List.of()
                        : request.answers().stream()
                        .map(answer -> new SubmitSurveyAnswerCommand(
                                answer.questionId(),
                                answer.rating(),
                                answer.comment()
                        ))
                        .toList()
        );

        SubmitSurveyResult result = submitSurveyUseCase.submitSurvey(command);
        return ResponseEntity.ok(toSubmitSurveyResponse(result));
    }

    private SurveyResponse toSurveyResponse(SurveySummaryResult result) {
        return new SurveyResponse(
                result.id(),
                result.title(),
                result.description(),
                result.startDate(),
                result.endDate(),
                result.createdBy(),
                result.status().name()
        );
    }

    private SurveyDetailResponse toSurveyDetailResponse(SurveyDetailResult result) {
        return new SurveyDetailResponse(
                result.id(),
                result.title(),
                result.description(),
                result.startDate(),
                result.endDate(),
                result.status().name(),
                result.questions().stream()
                        .map(this::toQuestionItemResponse)
                        .toList()
        );
    }

    private QuestionItemResponse toQuestionItemResponse(QuestionItemResult result) {
        return new QuestionItemResponse(
                result.id(),
                result.content(),
                result.type().name()
        );
    }

    private SubmitSurveyResponse toSubmitSurveyResponse(SubmitSurveyResult result) {
        return new SubmitSurveyResponse(
                result.success(),
                SubmitSurveyResponseCode.valueOf(result.code().name()),
                result.message()
        );
    }
}
