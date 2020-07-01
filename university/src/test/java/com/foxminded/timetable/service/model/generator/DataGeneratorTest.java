package com.foxminded.timetable.service.model.generator;

import com.foxminded.timetable.dao.ResetSequencesDao;
import com.foxminded.timetable.service.TimetableFacade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class DataGeneratorTest {

    @Mock
    private TimetableFacade timetableFacade;
    @Mock
    private TimetableModelGenerator  timetableModelGenerator;
    @Mock
    private UniversityModelGenerator universityModelGenerator;
    @Mock
    private ResetSequencesDao resetSequencesDao;

    @InjectMocks
    private DataGenerator dataGenerator;

    @Test
    public void refreshTimetableDataShouldEraseAndGenerateNewTimetableData() {

        dataGenerator.refreshTimetableData();

        then(timetableFacade).should().deleteTimetableData();
        then(timetableModelGenerator).should().generateAndSave();
        then(timetableFacade).shouldHaveNoMoreInteractions();
        then(timetableModelGenerator).shouldHaveNoMoreInteractions();
        then(universityModelGenerator).shouldHaveNoInteractions();
    }

    @Test
    public void refreshAllDataShouldEraseAllDataAndGenerateNewUniversityAndTimetableData() {

        dataGenerator.refreshAllData();

        then(timetableFacade).should().deleteAllData();
        then(universityModelGenerator).should().generateAndSave();
        then(timetableModelGenerator).should().generateAndSave();
        then(timetableFacade).shouldHaveNoMoreInteractions();
        then(universityModelGenerator).shouldHaveNoMoreInteractions();
        then(timetableModelGenerator).shouldHaveNoMoreInteractions();
    }

    @Test
    public void resetSequenceShouldDelegateDao() {

        dataGenerator.resetSequences();

        then(resetSequencesDao).should().resetSequences();
        then(resetSequencesDao).shouldHaveNoMoreInteractions();
        then(timetableFacade).shouldHaveNoInteractions();
        then(universityModelGenerator).shouldHaveNoInteractions();
        then(timetableModelGenerator).shouldHaveNoInteractions();
    }

}