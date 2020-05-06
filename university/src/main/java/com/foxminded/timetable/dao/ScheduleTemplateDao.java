package com.foxminded.timetable.dao;

import java.time.DayOfWeek;
import java.util.List;

import com.foxminded.timetable.model.ScheduleTemplate;

public interface ScheduleTemplateDao extends GenericDao<ScheduleTemplate> {

    List<ScheduleTemplate> findAllByDate(boolean weekParity, DayOfWeek day);

    ScheduleTemplate save(ScheduleTemplate template);

    ScheduleTemplate update(ScheduleTemplate template);

}
