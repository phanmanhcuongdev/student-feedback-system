package com.ttcs.backend.adapter.out.ai;

import com.ttcs.backend.application.port.out.GenerateTextEmbeddingPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class LocalTextEmbeddingAdapter implements GenerateTextEmbeddingPort {

    private final RestClient restClient;
    private final boolean enabled;
    private final String modelName;

    public LocalTextEmbeddingAdapter(
            RestClient.Builder restClientBuilder,
            @Value("${app.ai.embedding.enabled:false}") boolean enabled,
            @Value("${app.ai.embedding.base-url:http://localhost:8000}") String baseUrl,
            @Value("${app.ai.embedding.model:intfloat/multilingual-e5-small}") String modelName,
            @Value("${app.ai.embedding.timeout.connect-ms:2000}") long connectTimeoutMs,
            @Value("${app.ai.embedding.timeout.read-ms:10000}") long readTimeoutMs
    ) {
        this.enabled = enabled;
        this.modelName = modelName;
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(Math.max(1, connectTimeoutMs)))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(Math.max(1, readTimeoutMs)));
        this.restClient = restClientBuilder.clone()
                .requestFactory(requestFactory)
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public TextEmbeddingResult embed(List<String> texts) {
        if (!enabled) {
            return new TextEmbeddingResult(modelName, List.of());
        }
        if (texts == null || texts.isEmpty()) {
            return new TextEmbeddingResult(modelName, List.of());
        }

        EmbeddingResponse response = restClient.post()
                .uri("/embed")
                .body(Map.of("texts", texts))
                .retrieve()
                .body(EmbeddingResponse.class);
        List<List<Double>> vectors = response == null || response.embeddings() == null
                ? List.of()
                : response.embeddings();
        return new TextEmbeddingResult(
                response != null && response.model() != null && !response.model().isBlank() ? response.model() : modelName,
                vectors
        );
    }

    private record EmbeddingResponse(
            List<List<Double>> embeddings,
            String model
    ) {
    }
}
