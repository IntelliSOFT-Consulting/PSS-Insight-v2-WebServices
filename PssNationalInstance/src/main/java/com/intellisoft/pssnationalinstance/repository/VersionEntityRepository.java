package com.intellisoft.pssnationalinstance.repository;

import com.intellisoft.pssnationalinstance.db.VersionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface VersionEntityRepository extends CrudRepository<VersionEntity, Long> {

    Optional<VersionEntity> findFirstByStatusOrderByCreatedAtDesc(String Status);
    Page<VersionEntity> findAll(Pageable pageable);

}
