package com.intellisoft.pssnationalinstance.repository;

import com.intellisoft.pssnationalinstance.db.VersionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface VersionEntityRepository extends CrudRepository<VersionEntity, Long> {
//    findFirstByOrderByCreatedAtDesc
//    Page<VersionEntity> findFirstByCreatedAt()
////    Page<VersionEntity> findFirstByOrderByCreatedAtDesc(String userId, boolean isRead, Pageable pageable);


    Optional<VersionEntity> findFirstByStatusOrderByCreatedAtDesc(String Status);
    Page<VersionEntity> findAll(Pageable pageable);

}
