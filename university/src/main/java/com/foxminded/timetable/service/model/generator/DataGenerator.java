package com.foxminded.timetable.service.model.generator;

import com.foxminded.timetable.dao.DataEraseDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class DataGenerator {

    private final DataEraseDao dataEraseDao;
    private final TimetableModelGenerator timetableModelGenerator;
    private final UniversityModelGenerator universityModelGenerator;

    public void refreshAllData() {

        dataEraseDao.eraseAllData();
        universityModelGenerator.generateAndSave();
        timetableModelGenerator.generateAndSave();
    }

    public void refreshTimetableData() {

        dataEraseDao.eraseTimetableData();
        timetableModelGenerator.generateAndSave();
    }

    public void resetSequences() {

        dataEraseDao.resetSequences();
    }

}
