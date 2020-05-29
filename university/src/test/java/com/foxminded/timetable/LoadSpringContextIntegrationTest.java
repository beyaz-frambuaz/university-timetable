package com.foxminded.timetable;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TimetableApp.class,
                      initializers =
                              ConfigFileApplicationContextInitializer.class)
public class LoadSpringContextIntegrationTest {

    @Test
    public void contextLoads() {

    }

}
