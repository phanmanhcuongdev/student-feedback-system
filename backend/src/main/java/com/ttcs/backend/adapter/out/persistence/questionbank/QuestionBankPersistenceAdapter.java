package com.ttcs.backend.adapter.out.persistence.questionbank;

import com.ttcs.backend.application.domain.model.QuestionBankEntry;
import com.ttcs.backend.application.port.out.admin.ManageQuestionBankPort;
import com.ttcs.backend.application.port.out.admin.QuestionBankSearchPage;
import com.ttcs.backend.application.port.out.admin.QuestionBankSearchQuery;
import com.ttcs.backend.common.PersistenceAdapter;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class QuestionBankPersistenceAdapter implements ManageQuestionBankPort {

    private final QuestionBankRepository questionBankRepository;

    @Override
    public QuestionBankSearchPage loadPage(QuestionBankSearchQuery query) {
        int safePage = Math.max(query.page(), 0);
        int safeSize = Math.min(Math.max(query.size(), 1), 100);
        var result = questionBankRepository.findAll(
                specification(query),
                PageRequest.of(
                        safePage,
                        safeSize,
                        Sort.by(Sort.Direction.DESC, "updatedAt").and(Sort.by(Sort.Direction.DESC, "createdAt"))
                )
        );

        return new QuestionBankSearchPage(
                result.getContent().stream().map(this::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @Override
    public Optional<QuestionBankEntry> loadById(Integer id) {
        return questionBankRepository.findById(id).map(this::toDomain);
    }

    @Override
    public QuestionBankEntry save(QuestionBankEntry entry) {
        return toDomain(questionBankRepository.save(toEntity(entry)));
    }

    private Specification<QuestionBankEntity> specification(QuestionBankSearchQuery query) {
        return (root, ignoredQuery, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (query.keyword() != null && !query.keyword().isBlank()) {
                String pattern = "%" + query.keyword().trim().toLowerCase() + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("content")), pattern),
                        builder.like(builder.lower(root.get("category")), pattern)
                ));
            }
            if (query.type() != null && !query.type().isBlank()) {
                predicates.add(builder.equal(root.get("type"), query.type().trim().toUpperCase()));
            }
            if (query.category() != null && !query.category().isBlank()) {
                predicates.add(builder.like(builder.lower(root.get("category")), "%" + query.category().trim().toLowerCase() + "%"));
            }
            if (query.active() != null) {
                predicates.add(builder.equal(root.get("active"), query.active()));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private QuestionBankEntry toDomain(QuestionBankEntity entity) {
        return new QuestionBankEntry(
                entity.getId(),
                entity.getContent(),
                entity.getType(),
                entity.getCategory(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private QuestionBankEntity toEntity(QuestionBankEntry entry) {
        QuestionBankEntity entity = new QuestionBankEntity();
        entity.setId(entry.id());
        entity.setContent(entry.content());
        entity.setType(entry.type());
        entity.setCategory(entry.category());
        entity.setActive(entry.active());
        entity.setCreatedAt(entry.createdAt());
        entity.setUpdatedAt(entry.updatedAt());
        return entity;
    }
}
