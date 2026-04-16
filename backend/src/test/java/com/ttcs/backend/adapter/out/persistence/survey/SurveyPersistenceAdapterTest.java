package com.ttcs.backend.adapter.out.persistence.survey;

import com.ttcs.backend.adapter.out.persistence.admin.AdminEntity;
import com.ttcs.backend.adapter.out.persistence.admin.AdminRepository;
import com.ttcs.backend.application.domain.model.Survey;
import com.ttcs.backend.application.domain.model.SurveyLifecycleState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SurveyPersistenceAdapterTest {

    @Mock
    private SurveyRepository surveyRepository;

    @Mock
    private AdminRepository adminRepository;

    @InjectMocks
    private SurveyPersistenceAdapter surveyPersistenceAdapter;

    @Test
    void shouldAttachManagedAdminReferenceWhenSavingSurvey() {
        Survey survey = new Survey(
                null,
                "Midterm Feedback",
                "Description",
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1),
                7,
                false,
                SurveyLifecycleState.DRAFT
        );
        AdminEntity managedAdmin = new AdminEntity();
        managedAdmin.setId(7);

        when(adminRepository.getReferenceById(7)).thenReturn(managedAdmin);
        when(surveyRepository.save(any(SurveyEntity.class))).thenAnswer(invocation -> {
            SurveyEntity entity = invocation.getArgument(0);
            entity.setId(11);
            return entity;
        });

        surveyPersistenceAdapter.save(survey);

        ArgumentCaptor<SurveyEntity> captor = ArgumentCaptor.forClass(SurveyEntity.class);
        verify(surveyRepository).save(captor.capture());
        assertSame(managedAdmin, captor.getValue().getCreatedBy());
    }
}
