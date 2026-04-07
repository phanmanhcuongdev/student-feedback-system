package com.ttcs.backend.adapter.out.persistence;

import com.ttcs.backend.application.port.out.auth.SendVerifyEmailPort;
import com.ttcs.backend.common.PersistenceAdapter;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class SmtpVerifyEmailAdapter implements SendVerifyEmailPort {

    private static final Logger log = LoggerFactory.getLogger(SmtpVerifyEmailAdapter.class);

    private final Optional<JavaMailSender> mailSender;

    @Override
    public void sendVerifyEmail(String toEmail, String verifyUrl) {
        if (mailSender.isEmpty()) {
            log.warn("Skipping verification email because JavaMailSender is not configured. toEmail={}, verifyUrl={}",
                    toEmail,
                    verifyUrl);
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.get().createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Xac nhan email dang ky tai khoan");

            String html = """
                    <html>
                      <body>
                        <p>Vui long xac nhan email dang ky tai khoan.</p>
                        <p><a href="%s">Bam vao day de xac nhan</a></p>
                      </body>
                    </html>
                    """.formatted(verifyUrl);

            helper.setText(html, true);
            mailSender.get().send(mimeMessage);
        } catch (Exception ex) {
            throw new RuntimeException("Khong gui duoc email xac nhan", ex);
        }
    }
}
