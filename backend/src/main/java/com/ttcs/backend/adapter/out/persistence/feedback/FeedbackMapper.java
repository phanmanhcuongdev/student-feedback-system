package com.ttcs.backend.adapter.out.persistence.feedback;

import com.ttcs.backend.adapter.out.persistence.student.StudentMapper;
import com.ttcs.backend.application.domain.model.Feedback;

public final class FeedbackMapper {

    private FeedbackMapper() {
    }

    public static Feedback toDomain(FeedbackEntity entity) {
        return new Feedback(
                entity.getId(),
                StudentMapper.toDomain(entity.getStudent()),
                entity.getTitle(),
                entity.getContent(),
                entity.getContentOriginal(),
                entity.getContentVi(),
                entity.getContentEn(),
                entity.getSourceLang(),
                entity.getModelInfo(),
                entity.isAutoTranslated(),
                entity.getCreatedAt()
        );
    }
}
