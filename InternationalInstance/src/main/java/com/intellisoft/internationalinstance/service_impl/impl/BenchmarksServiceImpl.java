package com.intellisoft.internationalinstance.service_impl.impl;

import com.intellisoft.internationalinstance.Results;
import com.intellisoft.internationalinstance.db.Benchmarks;
import com.intellisoft.internationalinstance.db.repso.BenchmarksRepository;
import com.intellisoft.internationalinstance.service_impl.service.BenchmarksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BenchmarksServiceImpl implements BenchmarksService {

    @Autowired
    BenchmarksRepository benchmarksRepository;

    @Override
    public Results saveBenchmark(Benchmarks benchmarks) {

        benchmarksRepository.save(benchmarks);
        return new Results(200, benchmarks);
    }
}
