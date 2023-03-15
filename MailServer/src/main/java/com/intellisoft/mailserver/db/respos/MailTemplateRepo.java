package com.intellisoft.mailserver.db.respos;

import com.intellisoft.mailserver.db.MailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MailTemplateRepo extends JpaRepository<MailTemplate, Long> {
}
