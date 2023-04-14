package com.intellisoft.pssnationalinstance.service_impl.impl;

import com.intellisoft.pssnationalinstance.DbAboutUs;
import com.intellisoft.pssnationalinstance.Results;
import com.intellisoft.pssnationalinstance.db.AboutUs;
import com.intellisoft.pssnationalinstance.repository.AboutUsRepository;
import com.intellisoft.pssnationalinstance.service_impl.service.AboutUsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AboutUsServiceImpl implements AboutUsService {

    private final AboutUsRepository aboutUsRepository;

    @Override
    public Results addAboutUs(DbAboutUs dbAboutUs) {

        AboutUs aboutUs = new AboutUs();
        aboutUs.setAboutUs(dbAboutUs.getAboutUs());
        aboutUs.setContactUs(dbAboutUs.getContactUs());

        return new Results(200, aboutUsRepository.save(aboutUs));
    }

    @Override
    public Results listAboutUs(Boolean isLatest, int size, int pageNo) {

        List<AboutUs> aboutUsList = getAboutUs(isLatest, size, pageNo);
        return new Results(200, aboutUsList);
    }

    public List<AboutUs> getAboutUs(Boolean isLatest, int size, int pageNo){
        List<AboutUs> aboutUsList = new ArrayList<>();
        if (isLatest){
            AboutUs aboutUs = aboutUsRepository.findLatestItem();
            aboutUsList.add(aboutUs);
        }else {
            aboutUsList = aboutUsRepository.findAll();
        }
        return aboutUsList;
    }

    @Override
    public Results aboutUsDetails(String id) {

        Optional<AboutUs> optionalAboutUs = aboutUsRepository.findById(Long.valueOf(id));
        if (optionalAboutUs.isPresent()){
            AboutUs aboutUs = optionalAboutUs.get();
            return new Results(200, aboutUs);
        }else {
            return new Results(400, "Resource not found");
        }

    }

    @Override
    public Results updateAboutUs(String id, DbAboutUs dbAboutUs) {

        Optional<AboutUs> optionalAboutUs = aboutUsRepository.findById(Long.valueOf(id));
        if (optionalAboutUs.isPresent()){
            AboutUs aboutUs = optionalAboutUs.get();
            if (dbAboutUs.getAboutUs() != null) aboutUs.setAboutUs(dbAboutUs.getAboutUs());
            if (dbAboutUs.getContactUs() != null) aboutUs.setContactUs(dbAboutUs.getContactUs());

            return new Results(200, aboutUsRepository.save(aboutUs));
        }

        return new Results(400, "Resource not found");
    }
}
