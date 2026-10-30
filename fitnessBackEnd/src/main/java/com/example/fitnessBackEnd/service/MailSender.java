package com.example.fitnessBackEnd.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
public class MailSender {

    private final JavaMailSender mail;
    @Value("${app.mail.from}") private String from;
    @Value("${app.mail.from-name:FitForge}") private String fromName;

    public void send(String to, EmailRenderer.Rendered r, String oneClickUnsubscribeUrl)
            throws MessagingException, UnsupportedEncodingException {
        MimeMessage msg = mail.createMimeMessage();
        MimeMessageHelper h = new MimeMessageHelper(msg, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");
        h.setFrom(from, fromName);
        h.setTo(to);
        h.setSubject(r.subject());
        h.setText(r.text(), r.html());                 // texto plano + HTML

        if (oneClickUnsubscribeUrl != null) {
            msg.addHeader("List-Unsubscribe", "<" + oneClickUnsubscribeUrl + ">");
            msg.addHeader("List-Unsubscribe-Post", "List-Unsubscribe=One-Click");
        }
        mail.send(msg);
    }
}