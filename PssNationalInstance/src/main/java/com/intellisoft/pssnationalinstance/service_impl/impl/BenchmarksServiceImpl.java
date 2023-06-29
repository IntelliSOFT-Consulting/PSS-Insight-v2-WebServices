package com.intellisoft.pssnationalinstance.service_impl.impl;

import com.intellisoft.pssnationalinstance.Results;
import com.intellisoft.pssnationalinstance.db.Benchmarks;
import com.intellisoft.pssnationalinstance.repository.BenchmarksRepository;
import com.intellisoft.pssnationalinstance.service_impl.service.BenchmarksService;
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
