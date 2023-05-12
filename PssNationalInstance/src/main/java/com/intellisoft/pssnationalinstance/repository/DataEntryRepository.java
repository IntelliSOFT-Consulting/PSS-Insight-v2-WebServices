package com.intellisoft.pssnationalinstance.repository;

import com.intellisoft.pssnationalinstance.db.DataEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface DataEntryRepository extends CrudRepository<DataEntry, Long> {
    Page<DataEntry> findAllByStatus(String status, Pageable pageable);
    Page<DataEntry> findAll(Pageable pageable);

}
