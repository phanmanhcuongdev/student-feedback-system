package com.ttcs.backend.application.port.out;

import com.ttcs.backend.application.domain.model.Question;

import java.util.List;

public interface SaveQuestionPort {
    void saveAll(List<Question> questions);
}
