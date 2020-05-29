package com.foxminded.timetable.service.model.generator;

import com.foxminded.timetable.dao.jdbc.JdbcDataRefreshDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
public class DataGenerator {

    private final UniversityModelGenerator universityModelGenerator;
    private final TimetableModelGenerator  timetableModelGenerator;
    private final JdbcDataRefreshDao       jdbcDataRefreshDao;

    @PostConstruct
    public void init() {

        universityModelGenerator.generateAndSave();
        timetableModelGenerator.generateAndSave();
    }

    public void refreshTimetableData() {

        jdbcDataRefreshDao.eraseTimetableData();
        timetableModelGenerator.generateAndSave();
    }

    public void refreshAllData() {

        jdbcDataRefreshDao.eraseAllData();
        universityModelGenerator.generateAndSave();
        timetableModelGenerator.generateAndSave();
    }


}
