package com.ttcs.backend.adapter.out.persistence.feedback;

import com.ttcs.backend.adapter.out.persistence.student.StudentRepository;
import com.ttcs.backend.application.port.out.LoadFeedbackQuery;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedbackPersistenceAdapterTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query itemsQuery;

    @Mock
    private Query countQuery;

    @InjectMocks
    private FeedbackPersistenceAdapter adapter;

    @Test
    void shouldJoinFeedbackStudentIdToStudentUserIdForStaffListing() {
        when(entityManager.createNativeQuery(anyString())).thenReturn(itemsQuery, countQuery);
        when(itemsQuery.setParameter(anyString(), any())).thenReturn(itemsQuery);
        when(itemsQuery.getResultList()).thenReturn(List.<Object[]>of(new Object[]{
                1,
                3,
                "Active Student",
                "student.active@university.edu",
                "Navigation issue",
                "Please simplify the sidebar.",
                Timestamp.valueOf(LocalDateTime.of(2026, 4, 20, 10, 0))
        }));
        when(countQuery.getSingleResult()).thenReturn(1);

        var result = adapter.loadPage(new LoadFeedbackQuery(null, null, null, 0, 10, "createdAt", "desc"));

        assertEquals(1, result.items().size());
        assertEquals(3, result.items().getFirst().studentId());
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(entityManager, org.mockito.Mockito.times(2)).createNativeQuery(sqlCaptor.capture());
        assertTrue(sqlCaptor.getAllValues().stream().allMatch(sql ->
                sql.contains("INNER JOIN Student s ON s.user_id = f.student_id")
                        && sql.contains("INNER JOIN [User] u ON u.user_id = s.user_id")
        ));
    }
}
