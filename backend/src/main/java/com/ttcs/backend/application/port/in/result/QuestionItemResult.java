package com.ttcs.backend.application.port.in.result;

import com.ttcs.backend.application.domain.model.QuestionType;

public record QuestionItemResult(
        Integer id,
        String content,
        QuestionType type
) {
}
