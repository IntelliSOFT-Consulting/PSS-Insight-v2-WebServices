package com.intellisoft.pssnationalinstance.repository;

import com.intellisoft.pssnationalinstance.db.DataEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface DataEntryRepository extends CrudRepository<DataEntry, Long> {
    Page<DataEntry> findAllByStatusAndDataEntryPersonId(String status, String dataEntryPersonId, Pageable pageable);
    Page<DataEntry> findAllByDataEntryPersonId(String dataEntryPersonId, Pageable pageable);

}
