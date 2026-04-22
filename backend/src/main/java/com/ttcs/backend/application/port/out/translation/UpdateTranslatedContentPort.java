package com.ttcs.backend.application.port.out.translation;

public interface UpdateTranslatedContentPort {
    boolean updateTranslatedContent(TranslatedContentUpdateCommand command);
}
