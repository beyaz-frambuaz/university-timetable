package com.foxminded.timetable.service.filter;

import com.foxminded.timetable.model.Schedule;

import java.util.function.Predicate;

public interface SchedulePredicate extends Predicate<Schedule> {

    String getCriteria();
}
