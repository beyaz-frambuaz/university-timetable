package com.foxminded.timetable;

import static org.mockito.BDDMockito.then;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.foxminded.timetable.model.generator.TimetableModelGenerator;
import com.foxminded.timetable.model.generator.UniversityModelGenerator;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TimetableApp.class, 
initializers = ConfigFileApplicationContextInitializer.class)
public class LoadSpringContextIntegrationTest {

    @SpyBean
    private UniversityModelGenerator universityModelGenerator;
    @SpyBean
    private TimetableModelGenerator timatableModelGenerator;
    
    @Test
    public void contextLoadsAndDataGetsGeneratedAndSavedBeforeAppRuns() {
        
        then(universityModelGenerator).should().generateAndSave();
        then(timatableModelGenerator).should().generateAndSave();
    }

}
