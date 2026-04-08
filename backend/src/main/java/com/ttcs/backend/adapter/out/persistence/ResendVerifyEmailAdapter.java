package com.ttcs.backend.adapter.out.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ttcs.backend.application.domain.exception.VerifyEmailDeliveryException;
import com.ttcs.backend.application.port.out.auth.SendVerifyEmailPort;
import com.ttcs.backend.common.PersistenceAdapter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@PersistenceAdapter
@RequiredArgsConstructor
public class ResendVerifyEmailAdapter implements SendVerifyEmailPort {

    private static final Logger log = LoggerFactory.getLogger(ResendVerifyEmailAdapter.class);

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${app.mail.resend.api-key:}")
    private String resendApiKey;

    @Value("${app.mail.resend.api-url:https://api.resend.com/emails}")
    private String resendApiUrl;

    @Value("${app.mail.from:noreply@cuongdso.id.vn}")
    private String fromEmail;

    @Override
    public void sendVerifyEmail(String toEmail, String verifyUrl) {
        if (resendApiKey == null || resendApiKey.isBlank()) {
            log.error("Resend API key is missing. Refusing to continue registration email delivery.");
            throw new VerifyEmailDeliveryException("Cau hinh gui email chua san sang.");
        }

        try {
            String html = """
                    <html>
                      <body style="font-family: Arial, sans-serif; color: #0f172a;">
                        <h2>Xac minh email tai khoan</h2>
                        <p>Ban vua dang ky tai khoan tren he thong khao sat phan hoi sinh vien.</p>
                        <p>Vui long bam vao lien ket ben duoi de xac minh email:</p>
                        <p><a href="%s">Xac minh email</a></p>
                        <p>Neu ban khong thuc hien thao tac nay, vui long bo qua email.</p>
                      </body>
                    </html>
                    """.formatted(verifyUrl);

            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "from", fromEmail,
                    "to", new String[]{toEmail},
                    "subject", "Xac minh email dang ky tai khoan",
                    "html", html
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(resendApiUrl))
                    .header("Authorization", "Bearer " + resendApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.error("Resend email delivery failed. status={}, body={}", response.statusCode(), response.body());
                throw new VerifyEmailDeliveryException("Khong the gui email xac minh luc nay.");
            }
        } catch (VerifyEmailDeliveryException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while sending verification email with Resend.", ex);
            throw new VerifyEmailDeliveryException("Khong the gui email xac minh luc nay.", ex);
        }
    }
}
