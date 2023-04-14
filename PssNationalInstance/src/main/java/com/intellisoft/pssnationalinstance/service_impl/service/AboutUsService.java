package com.intellisoft.pssnationalinstance.service_impl.service;

import com.intellisoft.pssnationalinstance.DbAboutUs;
import com.intellisoft.pssnationalinstance.Results;
import com.intellisoft.pssnationalinstance.db.AboutUs;

import java.util.List;

public interface AboutUsService {

    Results addAboutUs(DbAboutUs dbAboutUs);
    Results listAboutUs(Boolean isLatest, int size, int pageNo);
    List<AboutUs> getAboutUs(Boolean isLatest, int size, int pageNo);
    Results aboutUsDetails(String id);
    Results updateAboutUs(String id, DbAboutUs dbAboutUs);

}
