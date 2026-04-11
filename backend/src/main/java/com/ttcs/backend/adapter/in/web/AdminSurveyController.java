package com.ttcs.backend.adapter.in.web;

import com.ttcs.backend.adapter.in.web.dto.CreateSurveyRequest;
import com.ttcs.backend.application.domain.model.QuestionType;
import com.ttcs.backend.application.port.in.CreateSurveyUseCase;
import com.ttcs.backend.application.port.in.command.CreateQuestionCommand;
import com.ttcs.backend.application.port.in.command.CreateSurveyCommand;
import com.ttcs.backend.common.WebAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@WebAdapter
@RestController
@RequestMapping("/api/admin/surveys")
@RequiredArgsConstructor
public class AdminSurveyController {

    private final CreateSurveyUseCase createSurveyUseCase;
    private final CurrentStudentProvider currentStudentProvider;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createSurvey(@RequestBody CreateSurveyRequest request) {
        Integer adminId = currentStudentProvider.currentStudentId();

        List<CreateQuestionCommand> questionCommands = request.questions() == null ? List.of() :
                request.questions().stream()
                        .map(q -> new CreateQuestionCommand(q.content(), QuestionType.valueOf(q.type())))
                        .toList();

        CreateSurveyCommand command = new CreateSurveyCommand(
                request.title(),
                request.description(),
                request.startDate(),
                request.endDate(),
                adminId,
                questionCommands
        );

        Integer surveyId = createSurveyUseCase.createSurvey(command);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "surveyId", surveyId,
                "message", "Survey created successfully"
        ));
    }
}
