package com.ttcs.backend.adapter.in.web;

import com.ttcs.backend.adapter.in.web.dto.QuestionBankPageResponse;
import com.ttcs.backend.adapter.in.web.dto.QuestionBankRequest;
import com.ttcs.backend.adapter.in.web.dto.QuestionBankResponse;
import com.ttcs.backend.application.domain.service.QuestionBankService;
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
@RequestMapping("/api/admin/question-bank")
@RequiredArgsConstructor
public class QuestionBankController {

    private final QuestionBankService questionBankService;

    @GetMapping
    public ResponseEntity<QuestionBankPageResponse> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(questionBankService.list(keyword, type, category, active, page, size));
    }

    @PostMapping
    public ResponseEntity<QuestionBankResponse> create(@RequestBody QuestionBankRequest request) {
        return ResponseEntity.ok(questionBankService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuestionBankResponse> update(@PathVariable Integer id, @RequestBody QuestionBankRequest request) {
        return ResponseEntity.ok(questionBankService.update(id, request));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<QuestionBankResponse> archive(@PathVariable Integer id) {
        return ResponseEntity.ok(questionBankService.setActive(id, false));
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<QuestionBankResponse> restore(@PathVariable Integer id) {
        return ResponseEntity.ok(questionBankService.setActive(id, true));
    }
}
