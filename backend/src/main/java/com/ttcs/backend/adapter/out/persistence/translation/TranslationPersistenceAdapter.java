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
            case "SURVEY_TITLE" -> updateSurveyTitle(command);
            case "SURVEY_DESCRIPTION" -> updateSurveyDescription(command);
            case "SURVEY_RESPONSE" -> updateSurveyResponse(command);
            default -> false;
        };
    }

    private boolean updateFeedback(TranslatedContentUpdateCommand command) {
        Query update = entityManager.createNativeQuery("""
                UPDATE [dbo].[Feedback]
                SET
                    [content_vi] = COALESCE(:contentVi, [content_vi]),
                    [content_en] = COALESCE(:contentEn, [content_en]),
                    [content_translated] = COALESCE(
                        CASE
                            WHEN :sourceLang = 'vi' THEN :contentEn
                            WHEN :sourceLang = 'en' THEN :contentVi
                            ELSE COALESCE(:contentEn, :contentVi)
                        END,
                        [content_translated]
                    ),
                    [source_lang] = :sourceLang,
                    [target_lang] = CASE
                        WHEN :sourceLang = 'vi' AND :contentEn IS NOT NULL THEN 'en'
                        WHEN :sourceLang = 'en' AND :contentVi IS NOT NULL THEN 'vi'
                        WHEN :contentEn IS NOT NULL THEN 'en'
                        WHEN :contentVi IS NOT NULL THEN 'vi'
                        ELSE [target_lang]
                    END,
                    [model_info] = :modelInfo,
                    [is_auto_translated] = 1
                WHERE [feedback_id] = :entityId
                """);
        bindUpdateParameters(update, command);
        return update.executeUpdate() > 0;
    }

    private boolean updateQuestion(TranslatedContentUpdateCommand command) {
        Query update = entityManager.createNativeQuery("""
                UPDATE [dbo].[Question]
                SET
                    [content_vi] = COALESCE(:contentVi, [content_vi]),
                    [content_en] = COALESCE(:contentEn, [content_en]),
                    [content_translated] = COALESCE(
                        CASE
                            WHEN :sourceLang = 'vi' THEN :contentEn
                            WHEN :sourceLang = 'en' THEN :contentVi
                            ELSE COALESCE(:contentEn, :contentVi)
                        END,
                        [content_translated]
                    ),
                    [source_lang] = :sourceLang,
                    [target_lang] = CASE
                        WHEN :sourceLang = 'vi' AND :contentEn IS NOT NULL THEN 'en'
                        WHEN :sourceLang = 'en' AND :contentVi IS NOT NULL THEN 'vi'
                        WHEN :contentEn IS NOT NULL THEN 'en'
                        WHEN :contentVi IS NOT NULL THEN 'vi'
                        ELSE [target_lang]
                    END,
                    [model_info] = :modelInfo,
                    [is_auto_translated] = 1
                WHERE [question_id] = :entityId
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
                    [content_vi] = COALESCE(:contentVi, [content_vi]),
                    [content_en] = COALESCE(:contentEn, [content_en]),
                    [content_translated] = COALESCE(
                        CASE
                            WHEN :sourceLang = 'vi' THEN :contentEn
                            WHEN :sourceLang = 'en' THEN :contentVi
                            ELSE COALESCE(:contentEn, :contentVi)
                        END,
                        [content_translated]
                    ),
                    [source_lang] = :sourceLang,
                    [target_lang] = CASE
                        WHEN :sourceLang = 'vi' AND :contentEn IS NOT NULL THEN 'en'
                        WHEN :sourceLang = 'en' AND :contentVi IS NOT NULL THEN 'vi'
                        WHEN :contentEn IS NOT NULL THEN 'en'
                        WHEN :contentVi IS NOT NULL THEN 'vi'
                        ELSE [target_lang]
                    END,
                    [model_info] = :modelInfo,
                    [is_auto_translated] = 1
                WHERE [question_id] = :entityId
                """);
        bindUpdateParameters(update, command);
        return update.executeUpdate() > 0;
    }

    private boolean updateSurveyTitle(TranslatedContentUpdateCommand command) {
        Query update = entityManager.createNativeQuery("""
                UPDATE [dbo].[Survey]
                SET
                    [title_vi] = COALESCE(:contentVi, [title_vi]),
                    [title_en] = COALESCE(:contentEn, [title_en]),
                    [source_lang] = :sourceLang,
                    [model_info] = :modelInfo,
                    [is_auto_translated] = 1
                WHERE [survey_id] = :entityId
                """);
        bindUpdateParameters(update, command);
        return update.executeUpdate() > 0;
    }

    private boolean updateSurveyDescription(TranslatedContentUpdateCommand command) {
        Query update = entityManager.createNativeQuery("""
                UPDATE [dbo].[Survey]
                SET
                    [description_vi] = COALESCE(:contentVi, [description_vi]),
                    [description_en] = COALESCE(:contentEn, [description_en]),
                    [source_lang] = :sourceLang,
                    [model_info] = :modelInfo,
                    [is_auto_translated] = 1
                WHERE [survey_id] = :entityId
                """);
        bindUpdateParameters(update, command);
        return update.executeUpdate() > 0;
    }

    private boolean updateSurveyResponse(TranslatedContentUpdateCommand command) {
        Query update = entityManager.createNativeQuery("""
                UPDATE [dbo].[Response_Detail]
                SET
                    [comment_vi] = COALESCE(:contentVi, [comment_vi]),
                    [comment_en] = COALESCE(:contentEn, [comment_en]),
                    [source_lang] = :sourceLang,
                    [model_info] = :modelInfo,
                    [is_auto_translated] = 1
                WHERE [id] = :entityId
                """);
        bindUpdateParameters(update, command);
        return update.executeUpdate() > 0;
    }

    private void bindUpdateParameters(Query update, TranslatedContentUpdateCommand command) {
        update.setParameter("contentVi", command.translatedContentVi());
        update.setParameter("contentEn", command.translatedContentEn());
        update.setParameter("sourceLang", command.sourceLang());
        update.setParameter("modelInfo", command.modelInfo());
        update.setParameter("entityId", command.entityId());
    }

    private boolean surveyQuestionTableExists() {
        Query query = entityManager.createNativeQuery("SELECT CASE WHEN OBJECT_ID(N'[dbo].[Survey_Question]', N'U') IS NULL THEN 0 ELSE 1 END");
        Object result = query.getSingleResult();
        return result instanceof Number number && number.intValue() == 1;
    }
}
