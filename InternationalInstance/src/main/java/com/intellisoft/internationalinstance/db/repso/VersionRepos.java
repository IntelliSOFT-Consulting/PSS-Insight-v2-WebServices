package com.intellisoft.internationalinstance.db.repso;

import com.intellisoft.internationalinstance.db.VersionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VersionRepos extends JpaRepository<VersionEntity, Long> {
    @Override
    Optional<VersionEntity> findById(Long aLong);

    Page<VersionEntity> findByStatus(String status, Pageable pageable);
    List<VersionEntity> findByStatus(String status);
}
