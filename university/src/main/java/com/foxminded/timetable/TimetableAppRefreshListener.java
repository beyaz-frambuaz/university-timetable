package com.foxminded.timetable;

import com.foxminded.timetable.service.model.generator.DataGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class TimetableAppRefreshListener
        implements ApplicationListener<ContextRefreshedEvent> {

    private final DataGenerator dataGenerator;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        log.info("Refreshing all data...");
        dataGenerator.resetSequences();
        dataGenerator.refreshAllData();
    }

}
