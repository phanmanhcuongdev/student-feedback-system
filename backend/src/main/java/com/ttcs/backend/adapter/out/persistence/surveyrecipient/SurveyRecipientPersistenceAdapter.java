package com.ttcs.backend.adapter.out.persistence.surveyrecipient;

import com.ttcs.backend.adapter.out.persistence.student.StudentEntity;
import com.ttcs.backend.adapter.out.persistence.student.StudentRepository;
import com.ttcs.backend.adapter.out.persistence.survey.SurveyEntity;
import com.ttcs.backend.adapter.out.persistence.survey.SurveyRepository;
import com.ttcs.backend.application.domain.model.SurveyRecipient;
import com.ttcs.backend.application.port.out.LoadSurveyRecipientPort;
import com.ttcs.backend.application.port.out.SaveSurveyRecipientPort;
import com.ttcs.backend.common.PersistenceAdapter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class SurveyRecipientPersistenceAdapter implements LoadSurveyRecipientPort, SaveSurveyRecipientPort {

    private final SurveyRecipientRepository surveyRecipientRepository;
    private final SurveyRepository surveyRepository;
    private final StudentRepository studentRepository;

    @Override
    public Optional<SurveyRecipient> loadBySurveyIdAndStudentId(Integer surveyId, Integer studentId) {
        return surveyRecipientRepository.findBySurvey_IdAndStudent_Id(surveyId, studentId)
                .map(SurveyRecipientMapper::toDomain);
    }

    @Override
    public List<SurveyRecipient> loadBySurveyId(Integer surveyId) {
        return surveyRecipientRepository.findBySurvey_IdOrderByIdAsc(surveyId).stream()
                .map(SurveyRecipientMapper::toDomain)
                .toList();
    }

    @Override
    public List<SurveyRecipient> loadByStudentId(Integer studentId) {
        return surveyRecipientRepository.findByStudent_IdOrderByAssignedAtDesc(studentId).stream()
                .map(SurveyRecipientMapper::toDomain)
                .toList();
    }

    @Override
    public SurveyRecipient save(SurveyRecipient recipient) {
        return SurveyRecipientMapper.toDomain(surveyRecipientRepository.save(toEntity(recipient)));
    }

    @Override
    public List<SurveyRecipient> saveAll(List<SurveyRecipient> recipients) {
        return surveyRecipientRepository.saveAll(recipients.stream().map(this::toEntity).toList()).stream()
                .map(SurveyRecipientMapper::toDomain)
                .toList();
    }

    private SurveyRecipientEntity toEntity(SurveyRecipient recipient) {
        SurveyEntity survey = surveyRepository.findById(recipient.getSurveyId())
                .orElseThrow(() -> new IllegalArgumentException("Survey not found: " + recipient.getSurveyId()));
        StudentEntity student = studentRepository.findById(recipient.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + recipient.getStudentId()));
        return new SurveyRecipientEntity(
                recipient.getId(),
                survey,
                student,
                recipient.getAssignedAt(),
                recipient.getOpenedAt(),
                recipient.getSubmittedAt()
        );
    }
}
