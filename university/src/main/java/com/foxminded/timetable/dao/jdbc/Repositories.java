package com.foxminded.timetable.dao.jdbc;

import com.foxminded.timetable.dao.*;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class Repositories {

    private final AuditoriumDao         auditoriumRepository;
    private final CourseDao             courseRepository;
    private final ProfessorDao          professorRepository;
    private final GroupDao              groupRepository;
    private final StudentDao            studentRepository;
    private final ScheduleTemplateDao   templateRepository;
    private final ScheduleDao           scheduleRepository;
    private final ReschedulingOptionDao reschedulingOptionRepository;

}
