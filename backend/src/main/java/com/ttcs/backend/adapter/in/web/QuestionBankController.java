package com.ttcs.backend.adapter.in.web;

import com.ttcs.backend.adapter.in.web.dto.QuestionBankPageResponse;
import com.ttcs.backend.adapter.in.web.dto.QuestionBankRequest;
import com.ttcs.backend.adapter.in.web.dto.QuestionBankResponse;
import com.ttcs.backend.application.port.in.admin.CreateQuestionBankEntryUseCase;
import com.ttcs.backend.application.port.in.admin.GetQuestionBankEntriesQuery;
import com.ttcs.backend.application.port.in.admin.GetQuestionBankEntriesUseCase;
import com.ttcs.backend.application.port.in.admin.QuestionBankEntryCommand;
import com.ttcs.backend.application.port.in.admin.QuestionBankEntryPageResult;
import com.ttcs.backend.application.port.in.admin.QuestionBankEntryResult;
import com.ttcs.backend.application.port.in.admin.SetQuestionBankEntryActiveUseCase;
import com.ttcs.backend.application.port.in.admin.UpdateQuestionBankEntryUseCase;
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

    private final GetQuestionBankEntriesUseCase getQuestionBankEntriesUseCase;
    private final CreateQuestionBankEntryUseCase createQuestionBankEntryUseCase;
    private final UpdateQuestionBankEntryUseCase updateQuestionBankEntryUseCase;
    private final SetQuestionBankEntryActiveUseCase setQuestionBankEntryActiveUseCase;

    @GetMapping
    public ResponseEntity<QuestionBankPageResponse> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(toPageResponse(getQuestionBankEntriesUseCase.list(
                new GetQuestionBankEntriesQuery(keyword, type, category, active, page, size)
        )));
    }

    @PostMapping
    public ResponseEntity<QuestionBankResponse> create(@RequestBody QuestionBankRequest request) {
        return ResponseEntity.ok(toResponse(createQuestionBankEntryUseCase.create(toCommand(request))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuestionBankResponse> update(@PathVariable Integer id, @RequestBody QuestionBankRequest request) {
        return ResponseEntity.ok(toResponse(updateQuestionBankEntryUseCase.update(id, toCommand(request))));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<QuestionBankResponse> archive(@PathVariable Integer id) {
        return ResponseEntity.ok(toResponse(setQuestionBankEntryActiveUseCase.setActive(id, false)));
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<QuestionBankResponse> restore(@PathVariable Integer id) {
        return ResponseEntity.ok(toResponse(setQuestionBankEntryActiveUseCase.setActive(id, true)));
    }

    private QuestionBankEntryCommand toCommand(QuestionBankRequest request) {
        return new QuestionBankEntryCommand(
                request == null ? null : request.content(),
                request == null ? null : request.type(),
                request == null ? null : request.category()
        );
    }

    private QuestionBankPageResponse toPageResponse(QuestionBankEntryPageResult result) {
        return new QuestionBankPageResponse(
                result.items().stream().map(this::toResponse).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages()
        );
    }

    private QuestionBankResponse toResponse(QuestionBankEntryResult result) {
        return new QuestionBankResponse(
                result.id(),
                result.content(),
                result.type(),
                result.category(),
                result.active(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
