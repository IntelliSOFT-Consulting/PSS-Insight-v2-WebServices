package com.intellisoft.internationalinstance.db.repso;

import com.intellisoft.internationalinstance.db.MailConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MailConfigurationRepository extends JpaRepository<MailConfiguration, Long> {
    Optional<MailConfiguration> findByServerType(String serverType);
    Optional<MailConfiguration> findByIsActive(Boolean isActive);
}
