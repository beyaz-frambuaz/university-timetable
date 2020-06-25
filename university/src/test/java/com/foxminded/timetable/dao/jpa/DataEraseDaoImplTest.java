package com.foxminded.timetable.dao.jpa;

import com.foxminded.timetable.dao.DataEraseDao;
import com.foxminded.timetable.service.TimetableFacade;
import com.foxminded.timetable.service.model.generator.DataGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class DataEraseDaoImplTest {

    @Autowired
    private DataEraseDao repository;

    @Autowired
    private DataGenerator dataGenerator;

    @Autowired
    private TimetableFacade timetableFacade;

    @Test
    void eraseAllDataShouldEraseDataFromAllTables() {

        dataGenerator.refreshAllData();

        repository.eraseAllData();

        assertThat(timetableFacade.countAuditoriums()).isZero();
        assertThat(timetableFacade.countProfessors()).isZero();
        assertThat(timetableFacade.countStudents()).isZero();
        assertThat(timetableFacade.countOptions()).isZero();
        assertThat(timetableFacade.countCourses()).isZero();
        assertThat(timetableFacade.countTemplates()).isZero();
        assertThat(timetableFacade.countGroups()).isZero();
    }

    @Test
    void eraseTimetableDataShouldEraseDataOnlyFromTimetableRelatedTables() {

        dataGenerator.refreshAllData();

        repository.eraseTimetableData();

        assertThat(timetableFacade.countOptions()).isZero();
        assertThat(timetableFacade.countTemplates()).isZero();

        assertThat(timetableFacade.countAuditoriums()).isNotZero();
        assertThat(timetableFacade.countProfessors()).isNotZero();
        assertThat(timetableFacade.countStudents()).isNotZero();
        assertThat(timetableFacade.countCourses()).isNotZero();
        assertThat(timetableFacade.countGroups()).isNotZero();
    }

}