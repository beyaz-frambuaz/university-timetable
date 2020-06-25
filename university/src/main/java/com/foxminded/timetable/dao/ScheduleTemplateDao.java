package com.foxminded.timetable.dao;

import com.foxminded.timetable.model.ScheduleTemplate;

import java.time.DayOfWeek;
import java.util.List;

public interface ScheduleTemplateDao extends GenericDao<ScheduleTemplate> {

    List<ScheduleTemplate> findAllByDay(boolean weekParity, DayOfWeek day);

    List<ScheduleTemplate> findAllByWeek(boolean weekParity);

}
