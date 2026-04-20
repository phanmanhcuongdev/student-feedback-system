package com.ttcs.backend.adapter.out.persistence.questionbank;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface QuestionBankRepository extends JpaRepository<QuestionBankEntity, Integer>, JpaSpecificationExecutor<QuestionBankEntity> {
}
