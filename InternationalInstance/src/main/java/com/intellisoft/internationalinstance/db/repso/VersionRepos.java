package com.intellisoft.internationalinstance.db.repso;

import com.intellisoft.internationalinstance.db.VersionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VersionRepos extends JpaRepository<VersionEntity, Long> {
    @Override
    Optional<VersionEntity> findById(Long aLong);

    Page<VersionEntity> findByStatus(String status, Pageable pageable);
    List<VersionEntity> findAll(Specification<VersionEntity> spec, Pageable pageable);

    Page<VersionEntity> findAllByStatus(String status, Pageable pageable);
    Page<VersionEntity> findAll(Pageable pageable);

    long count();

}
