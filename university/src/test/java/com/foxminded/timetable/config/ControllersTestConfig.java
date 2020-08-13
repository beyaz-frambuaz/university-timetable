package com.foxminded.timetable.config;

import com.foxminded.timetable.api.rest.assemblers.*;
import com.foxminded.timetable.service.utility.SemesterCalendar;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class ControllersTestConfig {

    @Bean
    public SemesterCalendar semesterCalendar() {

        return new SemesterCalendar("2020-09-07", "2020-12-11");
    }

    @Bean
    public AuditoriumModelAssembler auditoriumModelAssembler() {

        return new AuditoriumModelAssembler();
    }

    @Bean
    public CourseModelAssembler courseModelAssembler() {

        return new CourseModelAssembler();
    }

    @Bean
    public GroupModelAssembler groupModelAssembler() {

        return new GroupModelAssembler();
    }

    @Bean
    public ProfessorModelAssembler professorModelAssembler() {

        return new ProfessorModelAssembler();
    }

    @Bean
    public ScheduleModelAssembler scheduleModelAssembler() {

        return new ScheduleModelAssembler();
    }

    @Bean
    public StudentModelAssembler studentModelAssembler() {

        return new StudentModelAssembler();
    }

    @Bean
    public TemplateModelAssembler templateModelAssembler() {

        return new TemplateModelAssembler();
    }

}
