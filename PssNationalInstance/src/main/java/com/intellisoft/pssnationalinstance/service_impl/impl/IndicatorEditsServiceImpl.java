package com.intellisoft.pssnationalinstance.service_impl.impl;

import com.intellisoft.pssnationalinstance.DbDetails;
import com.intellisoft.pssnationalinstance.DbIndicatorEdit;
import com.intellisoft.pssnationalinstance.Results;
import com.intellisoft.pssnationalinstance.db.IndicatorEdits;
import com.intellisoft.pssnationalinstance.repository.IndicatorEditsRepository;
import com.intellisoft.pssnationalinstance.service_impl.service.IndicatorEditsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Log4j2
@RequiredArgsConstructor
public class IndicatorEditsServiceImpl implements IndicatorEditsService {

    private final IndicatorEditsRepository indicatorEditsRepository;

    @Override
    public Results addEdit(DbIndicatorEdit dbIndicatorEdit) {

        String categoryId = dbIndicatorEdit.getCategoryId();
        String indicatorId = dbIndicatorEdit.getIndicatorId();
        String editValue = dbIndicatorEdit.getEditValue();
        String creatorId = dbIndicatorEdit.getCreatorId();
        IndicatorEdits indicatorEdits = new IndicatorEdits();

        Optional<IndicatorEdits> optionalIndicatorEdits = indicatorEditsRepository.findByCategoryIdAndIndicatorIdAndCreatorId(categoryId, indicatorId, creatorId);
        if (optionalIndicatorEdits.isPresent()) {
            indicatorEdits = optionalIndicatorEdits.get();
            indicatorEdits.setEdit(editValue);
        } else {
            indicatorEdits.setIndicatorId(indicatorId);
            indicatorEdits.setCategoryId(categoryId);
            indicatorEdits.setCreatorId(creatorId);
            indicatorEdits.setEdit(editValue);
        }
        indicatorEditsRepository.save(indicatorEdits);

        return new Results(200, new DbDetails("Indicator edit saved successfully."));
    }

    @Transactional
    @Override
    public void deleteEditByCategoryId(String creatorId) {
        indicatorEditsRepository.deleteAllByCreatorId(creatorId);
    }

    @Override
    public List<IndicatorEdits> getIndicatorEditsCategoryCreator(String categoryId, String creatorId) {
        return null;
    }

    @Override
    public List<IndicatorEdits> getIndicatorEditsCreator(String creatorId) {

        return indicatorEditsRepository.findByCreatorId(creatorId);
    }


}
