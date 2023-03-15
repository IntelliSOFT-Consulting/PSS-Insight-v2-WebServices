package com.intellisoft.mailserver.service;

import com.intellisoft.mailserver.db.MailTemplate;
import com.intellisoft.mailserver.db.respos.MailTemplateRepo;
import com.intellisoft.mailserver.model.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {
    private final MailTemplateRepo mailTemplateRepo;
    @Override
    public Response saveTemplate(String templateName, String templateContent) {
        mailTemplateRepo.save(MailTemplate.builder()
                        .templateContent(templateContent)
                        .name(templateName)
                .build());
        return Response.builder().status("200").message("Template saved successfully").build();
    }

    @Override
    public Response deleteTemplate(Long templateId) {
        mailTemplateRepo.deleteById(templateId);
        return Response.builder().status("200").message("Template deleted successfully").build();
    }

    @Override
    public MailTemplate getTemplate(Long templateId) {

         return mailTemplateRepo.getById(templateId);
    }

    @Override
    public MailTemplate updateTemplate(Long templateId, String templateContent) {
        return mailTemplateRepo.findById(templateId).map(mailTemplate -> {mailTemplate.setTemplateContent(templateContent);
            return mailTemplateRepo.save(mailTemplate);}).orElse(null);
    }

    @Override
    public Response deactivateTemplate(Long templateId) {
        mailTemplateRepo.findById(templateId).ifPresent(mailTemplate -> mailTemplate.setIsActive(false));
        return Response.builder().status("200").message("Template deactivated successfully").build();
    }
}
