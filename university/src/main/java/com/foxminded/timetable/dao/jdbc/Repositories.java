package com.foxminded.timetable.dao.jdbc;

import org.springframework.stereotype.Component;

import com.foxminded.timetable.dao.AuditoriumDao;
import com.foxminded.timetable.dao.CourseDao;
import com.foxminded.timetable.dao.GroupDao;
import com.foxminded.timetable.dao.ProfessorDao;
import com.foxminded.timetable.dao.ReschedulingOptionDao;
import com.foxminded.timetable.dao.ScheduleDao;
import com.foxminded.timetable.dao.ScheduleTemplateDao;
import com.foxminded.timetable.dao.StudentDao;

import lombok.Data;

@Data
@Component
public class Repositories {
    
    private final AuditoriumDao auditoriumRepository;
    private final CourseDao courseRepository;
    private final ProfessorDao professorRepository;
    private final GroupDao groupRepository;
    private final StudentDao studentRepository;
    private final ScheduleTemplateDao templateRepository;
    private final ScheduleDao scheduleRepository;
    private final ReschedulingOptionDao reschedulingOptionRepository;

}
