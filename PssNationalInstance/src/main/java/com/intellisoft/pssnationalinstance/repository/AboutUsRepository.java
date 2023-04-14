package com.intellisoft.pssnationalinstance.repository;

import com.intellisoft.pssnationalinstance.db.AboutUs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AboutUsRepository extends JpaRepository<AboutUs, Long> {

    @Query(value = "SELECT * FROM about_us ORDER BY id DESC LIMIT 1", nativeQuery = true)
    AboutUs findLatestItem();
}
