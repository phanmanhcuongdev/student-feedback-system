package com.ttcs.backend.application.domain.service;

import com.ttcs.backend.application.port.in.translation.ApplyTranslationResultCommand;
import com.ttcs.backend.application.port.out.translation.TranslatedContentUpdateCommand;
import com.ttcs.backend.application.port.out.translation.UpdateTranslatedContentPort;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TranslationResultServiceTest {

    @Test
    void shouldApplySurveyResponseTranslationResult() {
        RecordingUpdateTranslatedContentPort updatePort = new RecordingUpdateTranslatedContentPort();
        TranslationResultService service = new TranslationResultService(updatePort);

        boolean applied = service.apply(new ApplyTranslationResultCommand(
                77,
                "SURVEY_RESPONSE",
                "Phan hoi da duoc dich",
                "Translated response",
                "en",
                "gpt-test",
                true
        ));

        assertTrue(applied);
        assertEquals(1, updatePort.calls);
        assertEquals("SURVEY_RESPONSE", updatePort.lastCommand.entityType());
        assertEquals(77, updatePort.lastCommand.entityId());
        assertEquals("Phan hoi da duoc dich", updatePort.lastCommand.translatedContentVi());
        assertEquals("Translated response", updatePort.lastCommand.translatedContentEn());
    }

    @Test
    void shouldRejectUnsupportedEntityType() {
        RecordingUpdateTranslatedContentPort updatePort = new RecordingUpdateTranslatedContentPort();
        TranslationResultService service = new TranslationResultService(updatePort);

        boolean applied = service.apply(new ApplyTranslationResultCommand(
                77,
                "UNKNOWN_ENTITY",
                "vi",
                "en",
                "en",
                "gpt-test",
                true
        ));

        assertFalse(applied);
        assertEquals(0, updatePort.calls);
    }

    private static final class RecordingUpdateTranslatedContentPort implements UpdateTranslatedContentPort {
        private int calls;
        private TranslatedContentUpdateCommand lastCommand;

        @Override
        public boolean updateTranslatedContent(TranslatedContentUpdateCommand command) {
            calls++;
            lastCommand = command;
            return true;
        }
    }
}
