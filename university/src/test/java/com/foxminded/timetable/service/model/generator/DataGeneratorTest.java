package com.foxminded.timetable.service.model.generator;

import com.foxminded.timetable.dao.DataEraseDao;
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
    private DataEraseDao             dataEraseDao;

    @InjectMocks
    private DataGenerator dataGenerator;

    @Test
    public void refreshTimetableDataShouldEraseAndGenerateNewTimetableData() {

        dataGenerator.refreshTimetableData();

        then(dataEraseDao).should().eraseTimetableData();
        then(timetableModelGenerator).should().generateAndSave();
        then(dataEraseDao).shouldHaveNoMoreInteractions();
        then(timetableModelGenerator).shouldHaveNoMoreInteractions();
        then(universityModelGenerator).shouldHaveNoMoreInteractions();
    }

    @Test
    public void refreshAllDataShouldEraseAllDataAndGenerateNewUniversityAndTimetableData() {

        dataGenerator.refreshAllData();

        then(dataEraseDao).should().eraseAllData();
        then(universityModelGenerator).should().generateAndSave();
        then(timetableModelGenerator).should().generateAndSave();
        then(dataEraseDao).shouldHaveNoMoreInteractions();
        then(universityModelGenerator).shouldHaveNoMoreInteractions();
        then(timetableModelGenerator).shouldHaveNoMoreInteractions();
    }

    @Test
    public void resetSequenceShouldDelegateDao() {

        dataGenerator.resetSequences();

        then(dataEraseDao).should().resetSequences();
        then(dataEraseDao).shouldHaveNoMoreInteractions();
        then(universityModelGenerator).shouldHaveNoMoreInteractions();
        then(timetableModelGenerator).shouldHaveNoMoreInteractions();
    }

}