package com.foxminded.timetable.service.utility.predicates;

import com.foxminded.timetable.model.Schedule;
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
