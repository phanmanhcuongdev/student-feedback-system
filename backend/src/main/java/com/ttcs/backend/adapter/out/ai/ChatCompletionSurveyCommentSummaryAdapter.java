package com.ttcs.backend.adapter.out.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ttcs.backend.application.port.out.GenerateSurveyCommentSummaryPort;
import com.ttcs.backend.application.port.out.SurveyCommentSummaryCommand;
import com.ttcs.backend.application.port.out.SurveyCommentSummaryResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ChatCompletionSurveyCommentSummaryAdapter implements GenerateSurveyCommentSummaryPort {

    private static final int MAX_CHARS_PER_CHUNK = 12000;
    private static final Pattern JSON_BLOCK_PATTERN = Pattern.compile("\\{.*}", Pattern.DOTALL);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;
    private final String baseUrl;

    public ChatCompletionSurveyCommentSummaryAdapter(
            ObjectMapper objectMapper,
            @Value("${app.ai.api-key:}") String apiKey,
            @Value("${app.ai.model:gemini-2.5-flash-lite}") String model,
            @Value("${app.ai.base-url:https://generativelanguage.googleapis.com/v1beta/openai/chat/completions}") String baseUrl
    ) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
        this.baseUrl = baseUrl;
        this.restClient = RestClient.builder().build();
    }

    @Override
    public SurveyCommentSummaryResult generateSummary(SurveyCommentSummaryCommand command) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("APP_AI_API_KEY is not configured");
        }

        List<String> chunks = chunkEntries(command.commentEntries());
        List<StructuredSummary> partials = new ArrayList<>();
        for (int index = 0; index < chunks.size(); index++) {
            partials.add(requestStructuredSummary(buildChunkPrompt(command, chunks.get(index), index + 1, chunks.size())));
        }

        StructuredSummary finalSummary = partials.size() == 1
                ? partials.getFirst()
                : requestStructuredSummary(buildMergePrompt(command, partials));

        return new SurveyCommentSummaryResult(
                model,
                finalSummary.summary(),
                finalSummary.highlights(),
                finalSummary.concerns(),
                finalSummary.actions()
        );
    }

    private List<String> chunkEntries(List<String> entries) {
        if (entries == null || entries.isEmpty()) {
            return List.of("No student text feedback was submitted for this survey.");
        }

        List<String> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String entry : entries) {
            if (current.length() > 0 && current.length() + entry.length() + 2 > MAX_CHARS_PER_CHUNK) {
                chunks.add(current.toString());
                current = new StringBuilder();
            }
            if (current.length() > 0) {
                current.append("\n\n");
            }
            current.append(entry);
        }
        if (current.length() > 0) {
            chunks.add(current.toString());
        }
        return chunks;
    }

    private String buildChunkPrompt(SurveyCommentSummaryCommand command, String chunk, int chunkIndex, int totalChunks) {
        return """
                Ban dang tom tat y kien sinh vien bang tieng Viet.
                Hay doc du lieu cua mot phan trong khao sat va tra ve JSON hop le theo dung schema:
                {
                  "summary": "string",
                  "highlights": ["string"],
                  "concerns": ["string"],
                  "actions": ["string"]
                }

                Yeu cau:
                - Tom tat ngan gon, ro rang, huong den admin truong hoc.
                - Khong nhac den ten sinh vien.
                - Khong chen markdown.
                - Chi duoc tra ve duy nhat mot object JSON, khong them giai thich nao khac.
                - Moi mang toi da 5 y.
                - Neu y kien it hoac khong ro rang, van tra ve JSON hop le va noi ro muc do tin cay thap trong summary.

                Tieu de khao sat: %s
                So luong comment text: %d
                Day la phan %d/%d cua tap du lieu.

                Du lieu:
                %s
                """.formatted(
                safe(command.surveyTitle()),
                command.commentCount(),
                chunkIndex,
                totalChunks,
                chunk
        );
    }

    private String buildMergePrompt(SurveyCommentSummaryCommand command, List<StructuredSummary> partials) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < partials.size(); index++) {
            StructuredSummary partial = partials.get(index);
            builder.append("Phan ").append(index + 1).append(":\n");
            builder.append("Tom tat: ").append(partial.summary()).append("\n");
            builder.append("Diem tich cuc: ").append(String.join("; ", partial.highlights())).append("\n");
            builder.append("Van de: ").append(String.join("; ", partial.concerns())).append("\n");
            builder.append("De xuat: ").append(String.join("; ", partial.actions())).append("\n\n");
        }

        return """
                Hay gop nhieu ban tom tat trung gian thanh mot ban tom tat cuoi cung bang tieng Viet.
                Tra ve JSON hop le theo schema:
                {
                  "summary": "string",
                  "highlights": ["string"],
                  "concerns": ["string"],
                  "actions": ["string"]
                }

                Yeu cau:
                - Khong lap lai y trung nhau.
                - Tong hop cho admin de doc nhanh.
                - Chi duoc tra ve duy nhat mot object JSON, khong them giai thich nao khac.
                - Moi mang toi da 5 y.

                Tieu de khao sat: %s
                So luong comment text: %d

                Cac ban tom tat trung gian:
                %s
                """.formatted(
                safe(command.surveyTitle()),
                command.commentCount(),
                builder
        );
    }

    private StructuredSummary requestStructuredSummary(String prompt) {
        String responseBody = restClient.post()
                .uri(baseUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(Map.of(
                        "model", model,
                        "temperature", 0.2,
                        "messages", List.of(
                                Map.of("role", "system", "content", "You summarize Vietnamese student feedback into concise valid JSON only."),
                                Map.of("role", "user", "content", prompt)
                        )
                ))
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String content = root.path("choices").path(0).path("message").path("content").asText();
            JsonNode json = objectMapper.readTree(extractJsonContent(content));

            return new StructuredSummary(
                    json.path("summary").asText(""),
                    readArray(json.path("highlights")),
                    readArray(json.path("concerns")),
                    readArray(json.path("actions"))
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to parse AI summary response", exception);
        }
    }

    private String extractJsonContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalStateException("AI provider returned empty content");
        }
        Matcher matcher = JSON_BLOCK_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group();
        }
        return content;
    }

    private List<String> readArray(JsonNode node) {
        if (node == null || !node.isArray()) {
            return List.of();
        }
        LinkedHashSet<String> values = new LinkedHashSet<>();
        for (JsonNode item : node) {
            String value = item.asText("").trim();
            if (!value.isBlank()) {
                values.add(value);
            }
        }
        return List.copyOf(values);
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "Khong co tieu de" : value;
    }

    private record StructuredSummary(
            String summary,
            List<String> highlights,
            List<String> concerns,
            List<String> actions
    ) {
    }
}
