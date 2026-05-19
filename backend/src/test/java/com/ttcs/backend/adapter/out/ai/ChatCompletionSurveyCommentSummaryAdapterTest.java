package com.ttcs.backend.adapter.out.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import com.ttcs.backend.application.port.out.SurveyCommentSummaryCommand;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatCompletionSurveyCommentSummaryAdapterTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String API_KEY = "test-api-key";
    private static final String MODEL = "gemini-test";

    @Test
    void shouldParseSuccessfulChatCompletionResponse() throws IOException {
        AtomicReference<String> authorizationHeader = new AtomicReference<>();
        AtomicReference<String> requestBody = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/chat/completions", exchange -> {
            authorizationHeader.set(exchange.getRequestHeaders().getFirst(HttpHeaders.AUTHORIZATION));
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] body = chatCompletionResponse("""
                    {"summary":"Students value clear lectures.","highlights":["Clear lectures"],"concerns":["Need more practice"],"actions":["Add exercises"]}
                    """).getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            exchange.sendResponseHeaders(HttpStatus.OK.value(), body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        try {
            ChatCompletionSurveyCommentSummaryAdapter adapter = adapter("http://localhost:" + server.getAddress().getPort() + "/chat/completions", 60000);

            var result = adapter.generateSummary(command());

            assertEquals(MODEL, result.modelName());
            assertEquals("Students value clear lectures.", result.summary());
            assertEquals(List.of("Clear lectures"), result.highlights());
            assertEquals(List.of("Need more practice"), result.concerns());
            assertEquals(List.of("Add exercises"), result.actions());
            assertEquals("Bearer " + API_KEY, authorizationHeader.get());
            assertTrue(requestBody.get().contains("[FEEDBACK]"));
            assertTrue(requestBody.get().contains("[/FEEDBACK]"));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void shouldThrowWhenProviderReturnsServerError() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/chat/completions", exchange -> {
            byte[] body = "server error".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(HttpStatus.INTERNAL_SERVER_ERROR.value(), body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        try {
            ChatCompletionSurveyCommentSummaryAdapter adapter = adapter("http://localhost:" + server.getAddress().getPort() + "/chat/completions", 60000);

            assertThrows(RuntimeException.class, () -> adapter.generateSummary(command()));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void shouldTimeoutWhenProviderResponseExceedsReadTimeout() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/chat/completions", exchange -> {
            try {
                Thread.sleep(250);
                byte[] body = chatCompletionResponse("""
                        {"summary":"late","highlights":[],"concerns":[],"actions":[]}
                        """).getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                exchange.sendResponseHeaders(HttpStatus.OK.value(), body.length);
                exchange.getResponseBody().write(body);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            } finally {
                exchange.close();
            }
        });
        server.start();
        try {
            ChatCompletionSurveyCommentSummaryAdapter adapter = adapter("http://localhost:" + server.getAddress().getPort() + "/chat/completions", 100);

            assertThrows(RuntimeException.class, () -> adapter.generateSummary(command()));
        } finally {
            server.stop(0);
        }
    }

    private ChatCompletionSurveyCommentSummaryAdapter adapter(String baseUrl, long readTimeoutMs) {
        return new ChatCompletionSurveyCommentSummaryAdapter(
                org.springframework.web.client.RestClient.builder(),
                baseUrl,
                API_KEY,
                MODEL,
                5000,
                readTimeoutMs,
                1,
                0
        );
    }

    private SurveyCommentSummaryCommand command() {
        return new SurveyCommentSummaryCommand(
                1,
                "Course survey",
                1,
                List.of("Giang vien giai thich ro rang va can them bai tap thuc hanh.")
        );
    }

    private String chatCompletionResponse(String contentJson) {
        return """
                {
                  "choices": [
                    {
                      "message": {
                        "content": %s
                      }
                    }
                  ]
                }
                """.formatted(quote(contentJson.strip()));
    }

    private String quote(String value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}
