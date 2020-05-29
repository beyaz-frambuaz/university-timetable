package com.foxminded.timetable.service.filter;

import com.foxminded.timetable.model.Schedule;

public class SchedulePredicateNoFilter implements SchedulePredicate {

    @Override
    public boolean test(Schedule schedule) {

        return true;
    }

    @Override
    public String getCriteria() {

        return "no filter";
    }

}
