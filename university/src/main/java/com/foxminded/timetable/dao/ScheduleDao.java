package com.foxminded.timetable.dao;

import com.foxminded.timetable.model.Schedule;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleDao extends GenericDao<Schedule> {

    List<Schedule> findAllByDate(LocalDate date);

    List<Schedule> findAllByTemplateId(long templateId);

    void updateAllWithTemplateId(Schedule schedule, int deltaDays);

    Schedule save(Schedule schedule);

    Schedule update(Schedule schedule);

}
