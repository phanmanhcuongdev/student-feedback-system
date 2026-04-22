package com.ttcs.backend.adapter.out.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import com.ttcs.backend.application.port.out.SurveyCommentSummaryCommand;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.web.client.MockRestServiceServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ChatCompletionSurveyCommentSummaryAdapterTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String BASE_URL = "https://gemini.test/v1beta/openai/chat/completions";
    private static final String API_KEY = "test-api-key";
    private static final String MODEL = "gemini-test";

    @Test
    void shouldParseSuccessfulChatCompletionResponse() {
        var builder = org.springframework.web.client.RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        ChatCompletionSurveyCommentSummaryAdapter adapter = new ChatCompletionSurveyCommentSummaryAdapter(
                builder,
                BASE_URL,
                API_KEY,
                MODEL
        );
        server.expect(once(), requestTo(BASE_URL))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + API_KEY))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("[FEEDBACK]")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("[/FEEDBACK]")))
                .andRespond(withSuccess(chatCompletionResponse("""
                        {"summary":"Students value clear lectures.","highlights":["Clear lectures"],"concerns":["Need more practice"],"actions":["Add exercises"]}
                        """), MediaType.APPLICATION_JSON));

        var result = adapter.generateSummary(command());

        assertEquals(MODEL, result.modelName());
        assertEquals("Students value clear lectures.", result.summary());
        assertEquals(List.of("Clear lectures"), result.highlights());
        assertEquals(List.of("Need more practice"), result.concerns());
        assertEquals(List.of("Add exercises"), result.actions());
        server.verify();
    }

    @Test
    void shouldThrowWhenProviderReturnsServerError() {
        var builder = org.springframework.web.client.RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        ChatCompletionSurveyCommentSummaryAdapter adapter = new ChatCompletionSurveyCommentSummaryAdapter(
                builder,
                BASE_URL,
                API_KEY,
                MODEL
        );
        server.expect(once(), requestTo(BASE_URL))
                .andRespond(withServerError());

        assertThrows(RuntimeException.class, () -> adapter.generateSummary(command()));
        server.verify();
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
            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(HttpClients.createDefault());
            requestFactory.setReadTimeout(Duration.ofMillis(100));
            ChatCompletionSurveyCommentSummaryAdapter adapter = new ChatCompletionSurveyCommentSummaryAdapter(
                    org.springframework.web.client.RestClient.builder().requestFactory(requestFactory),
                    "http://localhost:" + server.getAddress().getPort() + "/chat/completions",
                    API_KEY,
                    MODEL
            );

            assertThrows(RuntimeException.class, () -> adapter.generateSummary(command()));
        } finally {
            server.stop(0);
        }
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
