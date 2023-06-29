package com.intellisoft.internationalinstance.controller;

import com.intellisoft.internationalinstance.Results;
import com.intellisoft.internationalinstance.db.Benchmarks;
import com.intellisoft.internationalinstance.service_impl.service.BenchmarksService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.http.MediaType.ALL_VALUE;

@RequestMapping(value = "/api/v1/benchmarks")
@RestController
@RequiredArgsConstructor
public class BenchmarksController {
    @Autowired
    BenchmarksService benchmarksService;

    @Operation(summary = "Save new benchmark")
    @PostMapping(path = "/save", consumes = ALL_VALUE)
    public ResponseEntity<Results> saveBenchMark(@Valid @RequestBody Benchmarks benchmarks) {
        return ResponseEntity.status(HttpStatus.CREATED).body(benchmarksService.saveBenchmark(benchmarks));
    }
}