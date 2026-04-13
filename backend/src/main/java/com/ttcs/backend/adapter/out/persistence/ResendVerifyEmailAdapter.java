package com.ttcs.backend.adapter.out.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ttcs.backend.application.domain.exception.VerifyEmailDeliveryException;
import com.ttcs.backend.application.port.out.auth.SendPasswordResetEmailPort;
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
public class ResendVerifyEmailAdapter implements SendVerifyEmailPort, SendPasswordResetEmailPort {

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
        sendHtmlEmail(
                toEmail,
                "Xac minh email dang ky tai khoan",
                """
                        <html>
                          <body style="font-family: Arial, sans-serif; color: #0f172a;">
                            <h2>Xac minh email tai khoan</h2>
                            <p>Ban vua dang ky tai khoan tren he thong khao sat phan hoi sinh vien.</p>
                            <p>Vui long bam vao lien ket ben duoi de xac minh email:</p>
                            <p><a href="%s">Xac minh email</a></p>
                            <p>Neu ban khong thuc hien thao tac nay, vui long bo qua email.</p>
                          </body>
                        </html>
                        """.formatted(verifyUrl),
                "Khong the gui email xac minh luc nay."
        );
    }

    public void sendPasswordResetEmail(String toEmail, String resetUrl) {
        sendHtmlEmail(
                toEmail,
                "Dat lai mat khau",
                """
                        <html>
                          <body style="font-family: Arial, sans-serif; color: #0f172a;">
                            <h2>Dat lai mat khau</h2>
                            <p>He thong vua nhan yeu cau dat lai mat khau cho tai khoan cua ban.</p>
                            <p>Vui long bam vao lien ket ben duoi de dat lai mat khau:</p>
                            <p><a href="%s">Dat lai mat khau</a></p>
                            <p>Lien ket nay se het han sau mot khoang thoi gian ngan.</p>
                            <p>Neu ban khong yeu cau thao tac nay, vui long bo qua email.</p>
                          </body>
                        </html>
                        """.formatted(resetUrl),
                "Khong the gui email dat lai mat khau luc nay."
        );
    }

    private void sendHtmlEmail(String toEmail, String subject, String html, String failureMessage) {
        if (resendApiKey == null || resendApiKey.isBlank()) {
            log.error("Resend API key is missing. Refusing to send email. subject={}", subject);
            throw new VerifyEmailDeliveryException("Cau hinh gui email chua san sang.");
        }

        try {
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "from", fromEmail,
                    "to", new String[]{toEmail},
                    "subject", subject,
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
                log.error("Resend email delivery failed. subject={}, status={}, body={}", subject, response.statusCode(), response.body());
                throw new VerifyEmailDeliveryException(failureMessage);
            }
        } catch (VerifyEmailDeliveryException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while sending email with Resend. subject={}", subject, ex);
            throw new VerifyEmailDeliveryException(failureMessage, ex);
        }
    }
}
