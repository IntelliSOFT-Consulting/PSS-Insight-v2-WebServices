package com.intellisoft.mailserver.service;

import com.intellisoft.mailserver.model.Response;

import java.util.List;
import java.util.Map;

public interface SenderService {
    Response sendMail(List<Map<String,String>> emailsAndNames, String subject,Long templateId, Map<String, Object> variables);
}
