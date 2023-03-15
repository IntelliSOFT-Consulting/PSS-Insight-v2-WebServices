package com.intellisoft.mailserver.service;

import com.intellisoft.mailserver.db.MailTemplate;
import com.intellisoft.mailserver.model.Response;

public interface TemplateService {
Response saveTemplate(String templateName, String templateContent);
Response deleteTemplate(Long templateId);
MailTemplate getTemplate(Long templateId);
MailTemplate updateTemplate(Long templateId, String templateContent);
Response deactivateTemplate(Long templateId);
}
