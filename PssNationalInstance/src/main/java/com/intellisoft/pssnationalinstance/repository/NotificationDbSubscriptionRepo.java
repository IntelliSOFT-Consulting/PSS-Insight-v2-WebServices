package com.intellisoft.pssnationalinstance.repository;

import com.intellisoft.pssnationalinstance.db.NotificationDbSubscription;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationDbSubscriptionRepo extends JpaRepository<NotificationDbSubscription, Long> {
    Optional<NotificationDbSubscription> findByEmail(String email);
    List<NotificationDbSubscription> findAllByIsActive(boolean isActive);
    List<NotificationDbSubscription> findAllByIsActive(boolean isActive, Pageable pageable);
    Optional<NotificationDbSubscription> findByUserId(String userId);
    Optional<NotificationDbSubscription> findFirstByUserId(String userId);

    Optional<NotificationDbSubscription> findFirstByUserIdAndIsActiveTrue(String userId);

}
