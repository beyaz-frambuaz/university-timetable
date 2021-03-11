package com.shablii.timetable.service.model.generator;

import com.shablii.timetable.dao.ResetSequencesDao;
import com.shablii.timetable.service.TimetableFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class DataGenerator {

    private final TimetableFacade timetableFacade;
    private final ResetSequencesDao resetSequencesDao;
    private final TimetableModelGenerator timetableModelGenerator;
    private final UniversityModelGenerator universityModelGenerator;

    public void refreshAllData() {

        log.info("Refreshing all data...");
        timetableFacade.deleteAllData();
        universityModelGenerator.generateAndSave();
        timetableModelGenerator.generateAndSave();
    }

    public void refreshTimetableData() {

        log.info("Refreshing timetable data...");
        timetableFacade.deleteTimetableData();
        timetableModelGenerator.generateAndSave();
    }

    public void resetSequences() {

        log.info("Resetting sequences...");
        resetSequencesDao.resetSequences();
    }

}
