package com.shablii.timetable.service.utility.predicates;

import com.shablii.timetable.model.Schedule;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
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
