package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.adapter.in.web.dto.QuestionBankPageResponse;
import com.ttcs.backend.adapter.in.web.dto.QuestionBankRequest;
import com.ttcs.backend.adapter.in.web.dto.QuestionBankResponse;
import com.ttcs.backend.adapter.out.persistence.questionbank.QuestionBankEntity;
import com.ttcs.backend.adapter.out.persistence.questionbank.QuestionBankRepository;
import com.ttcs.backend.application.domain.model.QuestionType;
import com.ttcs.backend.common.UseCase;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@UseCase
@RequiredArgsConstructor
public class QuestionBankService {

    private final QuestionBankRepository questionBankRepository;

    @Transactional(readOnly = true)
    public QuestionBankPageResponse list(String keyword, String type, String category, Boolean active, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        var result = questionBankRepository.findAll(specification(keyword, type, category, active),
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "updatedAt").and(Sort.by(Sort.Direction.DESC, "createdAt"))));

        return new QuestionBankPageResponse(
                result.getContent().stream().map(this::toResponse).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @Transactional
    public QuestionBankResponse create(QuestionBankRequest request) {
        validate(request);
        QuestionBankEntity entity = new QuestionBankEntity();
        entity.setContent(request.content().trim());
        entity.setType(normalizeType(request.type()));
        entity.setCategory(normalizeNullable(request.category()));
        entity.setActive(true);
        entity.setCreatedAt(LocalDateTime.now());
        return toResponse(questionBankRepository.save(entity));
    }

    @Transactional
    public QuestionBankResponse update(Integer id, QuestionBankRequest request) {
        validate(request);
        QuestionBankEntity entity = questionBankRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("QUESTION_BANK_ENTRY_NOT_FOUND"));
        entity.setContent(request.content().trim());
        entity.setType(normalizeType(request.type()));
        entity.setCategory(normalizeNullable(request.category()));
        entity.setUpdatedAt(LocalDateTime.now());
        return toResponse(questionBankRepository.save(entity));
    }

    @Transactional
    public QuestionBankResponse setActive(Integer id, boolean active) {
        QuestionBankEntity entity = questionBankRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("QUESTION_BANK_ENTRY_NOT_FOUND"));
        entity.setActive(active);
        entity.setUpdatedAt(LocalDateTime.now());
        return toResponse(questionBankRepository.save(entity));
    }

    private Specification<QuestionBankEntity> specification(String keyword, String type, String category, Boolean active) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("content")), pattern),
                        builder.like(builder.lower(root.get("category")), pattern)
                ));
            }
            if (type != null && !type.isBlank()) {
                predicates.add(builder.equal(root.get("type"), normalizeType(type)));
            }
            if (category != null && !category.isBlank()) {
                predicates.add(builder.like(builder.lower(root.get("category")), "%" + category.trim().toLowerCase() + "%"));
            }
            if (active != null) {
                predicates.add(builder.equal(root.get("active"), active));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private void validate(QuestionBankRequest request) {
        if (request == null || request.content() == null || request.content().trim().isEmpty()) {
            throw new IllegalArgumentException("Question content is required.");
        }
        normalizeType(request.type());
    }

    private String normalizeType(String type) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Question type is required.");
        }
        return QuestionType.valueOf(type.trim().toUpperCase()).name();
    }

    private String normalizeNullable(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private QuestionBankResponse toResponse(QuestionBankEntity entity) {
        return new QuestionBankResponse(
                entity.getId(),
                entity.getContent(),
                entity.getType(),
                entity.getCategory(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
