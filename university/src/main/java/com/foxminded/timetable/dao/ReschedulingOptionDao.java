package com.foxminded.timetable.dao;

import com.foxminded.timetable.model.ReschedulingOption;
import com.foxminded.timetable.model.Schedule;

import java.time.LocalDate;
import java.util.List;

public interface ReschedulingOptionDao extends GenericDao<ReschedulingOption> {

    List<ReschedulingOption> findDayReschedulingOptionsForSchedule(
            boolean weekParity, LocalDate date, Schedule schedule);

}
