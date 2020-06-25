package com.foxminded.timetable.dao;

import com.foxminded.timetable.model.ReschedulingOption;

import java.time.DayOfWeek;
import java.util.List;

public interface ReschedulingOptionDao extends GenericDao<ReschedulingOption> {

    List<ReschedulingOption> findAllByDay(DayOfWeek dayOfWeek);

}
