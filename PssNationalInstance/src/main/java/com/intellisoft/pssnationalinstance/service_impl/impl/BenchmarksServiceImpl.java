package com.intellisoft.pssnationalinstance.service_impl.impl;

import com.intellisoft.pssnationalinstance.Results;
import com.intellisoft.pssnationalinstance.db.Benchmarks;
import com.intellisoft.pssnationalinstance.repository.BenchmarksRepository;
import com.intellisoft.pssnationalinstance.service_impl.service.BenchmarksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BenchmarksServiceImpl implements BenchmarksService {

    @Autowired
    BenchmarksRepository benchmarksRepository;

    @Override
    public Results saveBenchmark(Benchmarks benchmarks) {
        // Check if the record exists
        Optional<Benchmarks> existingBenchmark = benchmarksRepository.findByDataElementAndOrgUnit(benchmarks.getDataElement(), benchmarks.getOrgUnit());

        if (existingBenchmark.isPresent()) {
            // Update the existing record
            Benchmarks updatedBenchmark = existingBenchmark.get();

            // Access the individual fields
            String dataElement = benchmarks.getDataElement();
            String orgUnit = benchmarks.getOrgUnit();
            String period = benchmarks.getPeriod();
            String value = benchmarks.getValue();
            String indicatorCode = benchmarks.getIndicatorCode();
            String nationalValue = benchmarks.getNationalValue();

            // Compare and update the fields
            if (updatedBenchmark.getDataElement() == null) {
                updatedBenchmark.setDataElement(dataElement);
            }
            if (updatedBenchmark.getOrgUnit() == null) {
                updatedBenchmark.setOrgUnit(orgUnit);
            }
            if (updatedBenchmark.getPeriod() == null) {
                updatedBenchmark.setPeriod(period);
            }
            if (updatedBenchmark.getValue() == null) {
                updatedBenchmark.setValue(value);
            }
            if (updatedBenchmark.getIndicatorCode() == null) {
                updatedBenchmark.setIndicatorCode(indicatorCode);
            }
            if (updatedBenchmark.getNationalValue() == null) {
                updatedBenchmark.setNationalValue(nationalValue);
            }
            // Perform the update
            benchmarksRepository.save(updatedBenchmark);
        } else {
            // Create a new record
            benchmarksRepository.save(benchmarks);
            return new Results(200, benchmarks);
        }
        return new Results(200, benchmarks);
    }
}
