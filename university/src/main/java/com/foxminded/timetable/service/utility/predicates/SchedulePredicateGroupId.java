package com.foxminded.timetable.service.utility.predicates;

import com.foxminded.timetable.model.Schedule;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode
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
