package com.foxminded.timetable.dao;

import java.time.LocalDate;
import java.util.List;

import com.foxminded.timetable.model.ReschedulingOption;
import com.foxminded.timetable.model.Schedule;

public interface ReschedulingOptionDao extends GenericDao<ReschedulingOption> {

    List<ReschedulingOption> findDayReschedulingOptionsForSchedule(
            boolean weekParity, LocalDate date, Schedule schedule);

}
