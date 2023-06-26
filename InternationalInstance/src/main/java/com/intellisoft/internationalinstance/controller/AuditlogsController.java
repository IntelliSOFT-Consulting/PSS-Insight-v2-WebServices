package com.intellisoft.internationalinstance.controller;

import com.intellisoft.internationalinstance.Results;
import com.intellisoft.internationalinstance.db.Auditlogs;
import com.intellisoft.internationalinstance.service_impl.service.AuditlogsService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static org.springframework.http.MediaType.ALL_VALUE;

@RequestMapping(value = "/api/v1/change-logs")
@RestController
@RequiredArgsConstructor
public class AuditlogsController {
    @Autowired
    AuditlogsService auditlogsService;

    @Operation(summary = "Save new change log")
    @PostMapping(path = "/save", consumes = ALL_VALUE)
    public ResponseEntity<Results> saveAuditlog(@Valid @RequestBody Auditlogs auditlogs) {
        return ResponseEntity.status(HttpStatus.CREATED).body(auditlogsService.saveAuditLog(auditlogs));
    }

    @Operation(summary = "Get All Changelogs")
    @GetMapping(path = "/fetch", consumes = ALL_VALUE)
    public ResponseEntity<?> fetchAuditLogs() {
        return ResponseEntity.status(HttpStatus.FOUND).body(auditlogsService.fetchAuditLogs());
    }

    @Operation(summary = "Get Change log detail")
    @GetMapping(path = "/details/{version}", consumes = ALL_VALUE)
    public ResponseEntity<Results> getChangelogDetail(@PathVariable Long version) {
        return ResponseEntity.status(HttpStatus.FOUND).body(auditlogsService.getChangelogDetail(version));
    }
}
