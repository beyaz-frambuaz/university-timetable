package com.shablii.timetable.service.utility.predicates;

import com.shablii.timetable.constraints.IdValid;
import com.shablii.timetable.model.Schedule;
import lombok.*;

@EqualsAndHashCode
@RequiredArgsConstructor
public class SchedulePredicateGroupId implements SchedulePredicate {

    @IdValid("Group")
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
