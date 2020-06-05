package com.foxminded.timetable.controllers;

import com.foxminded.timetable.service.utility.SemesterCalendar;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class ControllersTestConfig {

    /* Adds calendar bean to context for Thymeleaf template rendering */
    @Bean
    public SemesterCalendar semesterCalendar() {

        return new SemesterCalendar("2020-09-07", "2020-12-11");
    }

}
