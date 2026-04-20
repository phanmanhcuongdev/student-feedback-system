package com.ttcs.backend.adapter.out.persistence.surveytemplate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SurveyTemplateRepository extends JpaRepository<SurveyTemplateEntity, Integer>, JpaSpecificationExecutor<SurveyTemplateEntity> {
}
