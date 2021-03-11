package com.shablii.timetable;

import com.shablii.timetable.service.model.generator.DataGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TimetableAppRefreshListener implements ApplicationListener<ContextRefreshedEvent> {

    private final DataGenerator dataGenerator;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        dataGenerator.resetSequences();
        dataGenerator.refreshAllData();
    }

}
