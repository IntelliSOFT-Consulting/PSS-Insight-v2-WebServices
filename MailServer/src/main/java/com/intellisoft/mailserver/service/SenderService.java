package com.intellisoft.mailserver.service;

import com.intellisoft.mailserver.model.Response;

import java.util.List;
import java.util.Map;

public interface SenderService {
    Response sendMail(List<String> to, String subject, String body, Map<String, Object> variables);
}
