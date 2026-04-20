package com.ttcs.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class SurveyAiSummaryExecutorConfig {

    @Bean(destroyMethod = "shutdown")
    public ExecutorService surveyAiSummaryExecutor() {
        return Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("survey-ai-summary-worker");
            thread.setDaemon(true);
            return thread;
        });
    }
}
