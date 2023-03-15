package com.intellisoft.mailserver.service;

import com.intellisoft.mailserver.model.Response;

import java.util.List;
import java.util.Map;

public class SenderServiceImpl implements SenderService {
    @Override
    public Response sendMail(List<String> to, String subject, String body, Map<String, Object> variables) {
        return null;
    }
    private String constructMessage(String message, Map<String, Object> variables) {
        if (variables != null) {
            variables.forEach((key, value) -> {
                message.replace("{"+key+"}", String.valueOf(value));
            });
        }
        return message;

    }
}
