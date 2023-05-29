package com.intellisoft.pssnationalinstance.repository;

import com.intellisoft.pssnationalinstance.db.DataEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DataEntryRepository extends CrudRepository<DataEntry, Long> {
    Page<DataEntry> findAllByStatus(String status, Pageable pageable);
    Page<DataEntry> findAll(Pageable pageable);
    Page<DataEntry> findAllByStatusAndDataEntryPersonId(String status, String dataEntryPersonId, Pageable pageable);

    List<DataEntry> findByDataEntryPersonId(String dataEntryPersonId);
    Page<DataEntry> findAllByDataEntryPersonId(String dataEntryPersonId, Pageable pageable);
}
