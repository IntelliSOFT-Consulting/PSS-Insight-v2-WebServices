package com.intellisoft.mailserver.controller;

import com.intellisoft.mailserver.db.MailTemplate;
import com.intellisoft.mailserver.model.Response;
import com.intellisoft.mailserver.model.TemplateModel;
import com.intellisoft.mailserver.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequestMapping(value = "/api/v1/mail-service/templates/")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequiredArgsConstructor
public class TemplateController {
    private final TemplateService templateService;
    @PostMapping("create")
    public Response createTemplate(@RequestBody TemplateModel templateModel) {
        return templateService.saveTemplate(templateModel.getTemplateName(), templateModel.getTemplateContent());

    }
    @DeleteMapping("delete/{templateId}")
    public Response deleteTemplate(@PathVariable Long templateId) {
        return templateService.deleteTemplate(templateId);
    }
    @PutMapping("update/{templateId}")
    public MailTemplate updateTemplate(@PathVariable Long templateId, @RequestBody TemplateModel templateModel) {
        return templateService.updateTemplate(templateId, templateModel.getTemplateContent());
    }
    @PutMapping("deactivate/{templateId}")
    public Response deactivateTemplate(@PathVariable Long templateId) {
        return templateService.deactivateTemplate(templateId);
    }
    @GetMapping("get/{templateId}")
    public MailTemplate getTemplate(@PathVariable Long templateId) {
        return templateService.getTemplate(templateId);
    }
}
