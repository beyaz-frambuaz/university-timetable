package com.shablii.timetable.service.utility.predicates;

import com.shablii.timetable.model.Schedule;

import java.util.function.Predicate;

public interface SchedulePredicate extends Predicate<Schedule> {

    String getCriteria();

}
