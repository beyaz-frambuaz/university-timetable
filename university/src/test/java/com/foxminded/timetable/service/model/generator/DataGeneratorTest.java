package com.foxminded.timetable.service.model.generator;

import com.foxminded.timetable.dao.jdbc.JdbcDataRefreshDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class DataGeneratorTest {

    @Mock
    private TimetableModelGenerator  timetableModelGenerator;
    @Mock
    private UniversityModelGenerator universityModelGenerator;
    @Mock
    private JdbcDataRefreshDao       jdbcDataRefreshDao;

    @InjectMocks
    private DataGenerator dataGenerator;

    @Test
    void refreshTimetableDataShouldEraseAndGenerateNewTimetableData() {

        dataGenerator.refreshTimetableData();

        then(jdbcDataRefreshDao).should().eraseTimetableData();
        then(timetableModelGenerator).should().generateAndSave();
        then(jdbcDataRefreshDao).shouldHaveNoMoreInteractions();
        then(timetableModelGenerator).shouldHaveNoMoreInteractions();
        then(universityModelGenerator).shouldHaveNoMoreInteractions();
    }

    @Test
    void refreshAllDataShouldEraseAllDataAndGenerateNewUniversityAndTimetableData() {

        dataGenerator.refreshAllData();
        then(jdbcDataRefreshDao).should().eraseAllData();
        then(universityModelGenerator).should().generateAndSave();
        then(timetableModelGenerator).should().generateAndSave();
        then(jdbcDataRefreshDao).shouldHaveNoMoreInteractions();
        then(universityModelGenerator).shouldHaveNoMoreInteractions();
        then(timetableModelGenerator).shouldHaveNoMoreInteractions();
    }

}