package com.ttcs.backend.adapter.in.web;

import com.ttcs.backend.adapter.in.web.dto.SurveyTemplatePageResponse;
import com.ttcs.backend.adapter.in.web.dto.SurveyTemplateRequest;
import com.ttcs.backend.adapter.in.web.dto.SurveyTemplateResponse;
import com.ttcs.backend.application.domain.service.SurveyTemplateService;
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

@WebAdapter
@RestController
@RequestMapping("/api/admin/survey-templates")
@RequiredArgsConstructor
public class SurveyTemplateController {

    private final SurveyTemplateService surveyTemplateService;

    @GetMapping
    public ResponseEntity<SurveyTemplatePageResponse> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(surveyTemplateService.list(keyword, active, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SurveyTemplateResponse> get(@PathVariable Integer id) {
        return ResponseEntity.ok(surveyTemplateService.get(id));
    }

    @PostMapping("/{id}/apply")
    public ResponseEntity<SurveyTemplateResponse> apply(@PathVariable Integer id) {
        return ResponseEntity.ok(surveyTemplateService.apply(id));
    }

    @PostMapping
    public ResponseEntity<SurveyTemplateResponse> create(@RequestBody SurveyTemplateRequest request) {
        return ResponseEntity.ok(surveyTemplateService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SurveyTemplateResponse> update(@PathVariable Integer id, @RequestBody SurveyTemplateRequest request) {
        return ResponseEntity.ok(surveyTemplateService.update(id, request));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<SurveyTemplateResponse> archive(@PathVariable Integer id) {
        return ResponseEntity.ok(surveyTemplateService.setActive(id, false));
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<SurveyTemplateResponse> restore(@PathVariable Integer id) {
        return ResponseEntity.ok(surveyTemplateService.setActive(id, true));
    }
}
