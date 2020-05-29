package com.foxminded.timetable.service.filter;

import com.foxminded.timetable.model.Schedule;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SchedulePredicateGroupId implements SchedulePredicate {

    private final long id;

    @Override
    public boolean test(Schedule schedule) {

        return schedule.getGroup().getId().equals(id);
    }

    @Override
    public String getCriteria() {

        return "group: " + id;
    }

}
