package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.domain.model.Question;
import com.ttcs.backend.application.domain.model.Survey;
import com.ttcs.backend.application.port.in.CreateSurveyUseCase;
import com.ttcs.backend.application.port.in.command.CreateSurveyCommand;
import com.ttcs.backend.application.port.out.SaveQuestionPort;
import com.ttcs.backend.application.port.out.SaveSurveyPort;
import com.ttcs.backend.common.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@UseCase
@RequiredArgsConstructor
public class CreateSurveyService implements CreateSurveyUseCase {

    private final SaveSurveyPort saveSurveyPort;
    private final SaveQuestionPort saveQuestionPort;

    @Override
    @Transactional
    public Integer createSurvey(CreateSurveyCommand command) {
        // Create and save Survey
        Survey survey = new Survey(
                null,
                command.title(),
                command.description(),
                command.startDate(),
                command.endDate(),
                command.createdBy()
        );
        Survey savedSurvey = saveSurveyPort.save(survey);

        // Create and save Questions with the newly generated Survey ID
        if (command.questions() != null && !command.questions().isEmpty()) {
            List<Question> questionsToSave = command.questions().stream()
                    .map(qCmd -> new Question(
                            null,
                            savedSurvey.getId(),
                            qCmd.content(),
                            qCmd.type()
                    ))
                    .toList();
            saveQuestionPort.saveAll(questionsToSave);
        }

        return savedSurvey.getId();
    }
}
