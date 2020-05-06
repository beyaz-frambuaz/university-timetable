package com.foxminded.timetable;

import javax.annotation.PostConstruct;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.foxminded.timetable.service.menu.MenuManager;
import com.foxminded.timetable.service.model.generator.TimetableModelGenerator;
import com.foxminded.timetable.service.model.generator.UniversityModelGenerator;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MainMenuRunner implements ApplicationRunner {
    
    private final UniversityModelGenerator universityModelGenerator;
    private final TimetableModelGenerator timatableModelGenerator;
    private final MenuManager menuManager;

    @PostConstruct
    private void populateData() {
        universityModelGenerator.generateAndSave();
        timatableModelGenerator.generateAndSave();
    }
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        while (menuManager.loadMainMenu());
    }
}
