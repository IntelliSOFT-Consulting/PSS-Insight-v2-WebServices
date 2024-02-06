package com.intellisoft.internationalinstance.db.repso;

import com.intellisoft.internationalinstance.db.NotificationSubscription;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationSubscriptionRepo extends JpaRepository<NotificationSubscription, Long> {
    Optional<NotificationSubscription> findByEmail(String email);
    List<NotificationSubscription> findAllByIsActive(boolean isActive);
    List<NotificationSubscription> findAllByIsActive(boolean isActive, Pageable pageable);
    Optional<NotificationSubscription> findByUserId(String userId);
    Optional<NotificationSubscription> findFirstByUserId(String userId);

    Optional<NotificationSubscription> findFirstByUserIdAndIsActiveTrue(String userId);

}
