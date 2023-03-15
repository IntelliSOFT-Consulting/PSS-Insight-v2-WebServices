package com.intellisoft.internationalinstance.db.repso;

import com.intellisoft.internationalinstance.db.NotificationSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationSubscriptionRepo extends JpaRepository<NotificationSubscription, Long> {
    Optional<NotificationSubscription> findByEmail(String email);
}
