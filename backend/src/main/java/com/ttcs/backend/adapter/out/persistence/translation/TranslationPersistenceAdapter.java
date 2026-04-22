package com.ttcs.backend.adapter.out.persistence.translation;

import com.ttcs.backend.application.port.out.translation.TranslatedContentUpdateCommand;
import com.ttcs.backend.application.port.out.translation.UpdateTranslatedContentPort;
import com.ttcs.backend.common.PersistenceAdapter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;

@PersistenceAdapter
@RequiredArgsConstructor
public class TranslationPersistenceAdapter implements UpdateTranslatedContentPort {

    private final EntityManager entityManager;

    @Override
    public boolean updateTranslatedContent(TranslatedContentUpdateCommand command) {
        return switch (command.entityType()) {
            case "FEEDBACK" -> updateFeedback(command);
            case "QUESTION" -> updateQuestion(command);
            case "SURVEY_QUESTION" -> updateSurveyQuestion(command);
            default -> false;
        };
    }

    private boolean updateFeedback(TranslatedContentUpdateCommand command) {
        Query update = entityManager.createNativeQuery("""
                UPDATE [dbo].[Feedback]
                SET
                    [content_translated] = :contentTranslated,
                    [source_lang] = :sourceLang,
                    [target_lang] = :targetLang,
                    [model_info] = :modelInfo,
                    [is_auto_translated] = 1
                WHERE [entity_type] = 'FEEDBACK'
                  AND [entity_id] = :entityId
                """);
        bindUpdateParameters(update, command);
        return update.executeUpdate() > 0;
    }

    private boolean updateQuestion(TranslatedContentUpdateCommand command) {
        Query update = entityManager.createNativeQuery("""
                UPDATE [dbo].[Question]
                SET
                    [content_translated] = :contentTranslated,
                    [source_lang] = :sourceLang,
                    [target_lang] = :targetLang,
                    [model_info] = :modelInfo,
                    [is_auto_translated] = 1
                WHERE [entity_type] = 'QUESTION'
                  AND [entity_id] = :entityId
                """);
        bindUpdateParameters(update, command);
        return update.executeUpdate() > 0;
    }

    private boolean updateSurveyQuestion(TranslatedContentUpdateCommand command) {
        if (!surveyQuestionTableExists()) {
            return false;
        }

        Query update = entityManager.createNativeQuery("""
                UPDATE [dbo].[Survey_Question]
                SET
                    [content_translated] = :contentTranslated,
                    [source_lang] = :sourceLang,
                    [target_lang] = :targetLang,
                    [model_info] = :modelInfo,
                    [is_auto_translated] = 1
                WHERE [entity_type] = 'SURVEY_QUESTION'
                  AND [entity_id] = :entityId
                """);
        bindUpdateParameters(update, command);
        return update.executeUpdate() > 0;
    }

    private void bindUpdateParameters(Query update, TranslatedContentUpdateCommand command) {
        update.setParameter("contentTranslated", command.translatedContent());
        update.setParameter("sourceLang", command.sourceLang());
        update.setParameter("targetLang", command.targetLang());
        update.setParameter("modelInfo", command.modelInfo());
        update.setParameter("entityId", command.entityId());
    }

    private boolean surveyQuestionTableExists() {
        Query query = entityManager.createNativeQuery("SELECT CASE WHEN OBJECT_ID(N'[dbo].[Survey_Question]', N'U') IS NULL THEN 0 ELSE 1 END");
        Object result = query.getSingleResult();
        return result instanceof Number number && number.intValue() == 1;
    }
}
