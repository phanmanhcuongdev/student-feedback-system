package com.ttcs.backend.application.port.out.ai;

public interface SendTranslationTaskPort {
    void send(TranslationTaskCommand command);
}
