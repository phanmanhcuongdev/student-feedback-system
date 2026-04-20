package com.ttcs.backend.adapter.in.web;

import com.ttcs.backend.adapter.in.web.dto.SurveyTemplatePageResponse;
import com.ttcs.backend.adapter.in.web.dto.SurveyTemplateQuestionResponse;
import com.ttcs.backend.adapter.in.web.dto.SurveyTemplateRequest;
import com.ttcs.backend.adapter.in.web.dto.SurveyTemplateResponse;
import com.ttcs.backend.application.port.in.admin.ApplySurveyTemplateUseCase;
import com.ttcs.backend.application.port.in.admin.CreateSurveyTemplateUseCase;
import com.ttcs.backend.application.port.in.admin.GetSurveyTemplateUseCase;
import com.ttcs.backend.application.port.in.admin.GetSurveyTemplatesQuery;
import com.ttcs.backend.application.port.in.admin.GetSurveyTemplatesUseCase;
import com.ttcs.backend.application.port.in.admin.SetSurveyTemplateActiveUseCase;
import com.ttcs.backend.application.port.in.admin.SurveyTemplateCommand;
import com.ttcs.backend.application.port.in.admin.SurveyTemplatePageResult;
import com.ttcs.backend.application.port.in.admin.SurveyTemplateQuestionCommand;
import com.ttcs.backend.application.port.in.admin.SurveyTemplateQuestionResult;
import com.ttcs.backend.application.port.in.admin.SurveyTemplateResult;
import com.ttcs.backend.application.port.in.admin.UpdateSurveyTemplateUseCase;
import com.ttcs.backend.common.WebAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@WebAdapter
@RestController
@RequestMapping("/api/admin/survey-templates")
@RequiredArgsConstructor
public class SurveyTemplateController {

    private final GetSurveyTemplatesUseCase getSurveyTemplatesUseCase;
    private final GetSurveyTemplateUseCase getSurveyTemplateUseCase;
    private final ApplySurveyTemplateUseCase applySurveyTemplateUseCase;
    private final CreateSurveyTemplateUseCase createSurveyTemplateUseCase;
    private final UpdateSurveyTemplateUseCase updateSurveyTemplateUseCase;
    private final SetSurveyTemplateActiveUseCase setSurveyTemplateActiveUseCase;

    @GetMapping
    public ResponseEntity<SurveyTemplatePageResponse> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(toPageResponse(getSurveyTemplatesUseCase.list(
                new GetSurveyTemplatesQuery(keyword, active, page, size)
        )));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SurveyTemplateResponse> get(@PathVariable Integer id) {
        return ResponseEntity.ok(toResponse(getSurveyTemplateUseCase.get(id)));
    }

    @PostMapping("/{id}/apply")
    public ResponseEntity<SurveyTemplateResponse> apply(@PathVariable Integer id) {
        return ResponseEntity.ok(toResponse(applySurveyTemplateUseCase.apply(id)));
    }

    @PostMapping
    public ResponseEntity<SurveyTemplateResponse> create(@RequestBody SurveyTemplateRequest request) {
        return ResponseEntity.ok(toResponse(createSurveyTemplateUseCase.create(toCommand(request))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SurveyTemplateResponse> update(@PathVariable Integer id, @RequestBody SurveyTemplateRequest request) {
        return ResponseEntity.ok(toResponse(updateSurveyTemplateUseCase.update(id, toCommand(request))));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<SurveyTemplateResponse> archive(@PathVariable Integer id) {
        return ResponseEntity.ok(toResponse(setSurveyTemplateActiveUseCase.setActive(id, false)));
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<SurveyTemplateResponse> restore(@PathVariable Integer id) {
        return ResponseEntity.ok(toResponse(setSurveyTemplateActiveUseCase.setActive(id, true)));
    }

    private SurveyTemplateCommand toCommand(SurveyTemplateRequest request) {
        return new SurveyTemplateCommand(
                request == null ? null : request.name(),
                request == null ? null : request.description(),
                request == null ? null : request.suggestedTitle(),
                request == null ? null : request.suggestedSurveyDescription(),
                request == null ? null : request.recipientScope(),
                request == null ? null : request.recipientDepartmentId(),
                request == null || request.questions() == null
                        ? List.of()
                        : request.questions().stream()
                        .map(question -> new SurveyTemplateQuestionCommand(
                                question.questionBankEntryId(),
                                question.content(),
                                question.type()
                        ))
                        .toList()
        );
    }

    private SurveyTemplatePageResponse toPageResponse(SurveyTemplatePageResult result) {
        return new SurveyTemplatePageResponse(
                result.items().stream().map(this::toResponse).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages()
        );
    }

    private SurveyTemplateResponse toResponse(SurveyTemplateResult result) {
        return new SurveyTemplateResponse(
                result.id(),
                result.name(),
                result.description(),
                result.suggestedTitle(),
                result.suggestedSurveyDescription(),
                result.recipientScope(),
                result.recipientDepartmentId(),
                result.active(),
                result.createdAt(),
                result.updatedAt(),
                result.questions().stream().map(this::toQuestionResponse).toList()
        );
    }

    private SurveyTemplateQuestionResponse toQuestionResponse(SurveyTemplateQuestionResult result) {
        return new SurveyTemplateQuestionResponse(
                result.id(),
                result.questionBankEntryId(),
                result.content(),
                result.type(),
                result.displayOrder()
        );
    }
}
