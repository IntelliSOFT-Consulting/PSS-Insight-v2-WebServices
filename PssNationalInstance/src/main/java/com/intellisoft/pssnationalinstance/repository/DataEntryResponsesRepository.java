package com.intellisoft.pssnationalinstance.repository;

import com.intellisoft.pssnationalinstance.db.DataEntry;
import com.intellisoft.pssnationalinstance.db.DataEntryResponses;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DataEntryResponsesRepository extends CrudRepository<DataEntryResponses, Long> {
    List<DataEntryResponses>findByDataEntry(DataEntry dataEntry);
}
