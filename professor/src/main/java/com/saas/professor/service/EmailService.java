package com.saas.professor.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class EmailService {

    @Value("${resend.api-key}")
    private String apiKey;

    @Value("${resend.from}")
    private String from;

    @Value("${app.url}")
    private String appUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendVerificationEmail(String toEmail, String toName, String token) {
        String verifyUrl = appUrl + "/verify-email?token=" + token;

        String html = "<div style=\"font-family:'Segoe UI',sans-serif;max-width:520px;margin:0 auto;background:#f8fafc;padding:32px;\">"
            + "<div style=\"background:white;border-radius:16px;padding:36px;box-shadow:0 2px 8px rgba(0,0,0,0.06);\">"
            + "<div style=\"margin-bottom:28px;\">"
            + "<span style=\"font-size:24px;font-weight:800;color:#2563eb;\">&#127891; NotaF&#225;cil</span>"
            + "</div>"
            + "<h2 style=\"font-size:22px;font-weight:800;color:#0f172a;margin-bottom:10px;\">Confirme seu e-mail &#128231;</h2>"
            + "<p style=\"color:#64748b;font-size:15px;margin-bottom:28px;line-height:1.6;\">"
            + "Ol&#225;, <strong>" + toName + "</strong>! Clique no bot&#227;o abaixo para ativar sua conta no NotaF&#225;cil."
            + "</p>"
            + "<a href=\"" + verifyUrl + "\" style=\"display:inline-block;background:linear-gradient(135deg,#2563eb,#06b6d4);color:white;text-decoration:none;padding:14px 32px;border-radius:10px;font-weight:700;font-size:15px;\">"
            + "&#9989; Confirmar e-mail"
            + "</a>"
            + "<p style=\"color:#94a3b8;font-size:12px;margin-top:28px;line-height:1.6;\">"
            + "Se voc&#234; n&#227;o criou uma conta no NotaF&#225;cil, ignore este e-mail.<br/>"
            + "Este link expira em 24 horas."
            + "</p>"
            + "</div></div>";

        sendEmail(toEmail, "Confirme seu e-mail - NotaFacil", html);
    }

    public void sendPasswordResetEmail(String to, String name, String token) {
        String resetUrl = appUrl + "/reset-password?token=" + token;

        String html = "<div style=\"font-family:sans-serif;max-width:520px;margin:0 auto;padding:32px;background:#f8fafc;border-radius:16px;\">"
            + "<div style=\"text-align:center;margin-bottom:24px;\">"
            + "<span style=\"font-size:32px;\">&#128273;</span>"
            + "<h1 style=\"color:#1e40af;font-size:22px;margin:8px 0;\">&#127891; NotaF&#225;cil</h1>"
            + "</div>"
            + "<p style=\"color:#374151;font-size:15px;\">Ol&#225;, <strong>" + name + "</strong>!</p>"
            + "<p style=\"color:#374151;font-size:15px;\">Recebemos uma solicita&#231;&#227;o para redefinir a senha da sua conta no <strong>NotaF&#225;cil</strong>.</p>"
            + "<div style=\"text-align:center;margin:28px 0;\">"
            + "<a href=\"" + resetUrl + "\" style=\"background:#2563eb;color:white;padding:14px 32px;border-radius:10px;text-decoration:none;font-weight:700;font-size:15px;\">"
            + "Redefinir minha senha"
            + "</a>"
            + "</div>"
            + "<p style=\"color:#6b7280;font-size:13px;\">Este link expira em <strong>1 hora</strong>. Se voc&#234; n&#227;o solicitou isso, ignore este e-mail.</p>"
            + "<hr style=\"border:none;border-top:1px solid #e5e7eb;margin:24px 0;\"/>"
            + "<p style=\"color:#9ca3af;font-size:12px;text-align:center;\">NotaF&#225;cil &mdash; Gest&#227;o escolar simplificada</p>"
            + "</div>";

        sendEmail(to, "Redefinir sua senha - NotaFacil", html);
    }

    private void sendEmail(String to, String subject, String html) {
        Map<String, Object> body = Map.of(
            "from", from,
            "to", new String[]{to},
            "subject", subject,
            "html", html
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        try {
            restTemplate.postForObject(
                "https://api.resend.com/emails",
                new HttpEntity<>(body, headers),
                String.class
            );
        } catch (Exception e) {
            System.err.println("Erro ao enviar e-mail para " + to + ": " + e.getMessage());
        }
    }
}