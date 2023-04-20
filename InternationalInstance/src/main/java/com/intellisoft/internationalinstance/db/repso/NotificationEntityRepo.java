package com.intellisoft.internationalinstance.db.repso;

import com.intellisoft.internationalinstance.db.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationEntityRepo extends JpaRepository<NotificationEntity, Long> {

    @Query("SELECT n FROM NotificationEntity n WHERE :emailAddress MEMBER OF n.emailList")
    Page<NotificationEntity> findByEmailAddressPage(@Param("emailAddress") String emailAddress, Pageable pageable);
    @Query("SELECT n FROM NotificationEntity n WHERE :emailAddress MEMBER OF n.emailList")
    List<NotificationEntity> findByEmailAddress(@Param("emailAddress") String emailAddress);
    List<NotificationEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
