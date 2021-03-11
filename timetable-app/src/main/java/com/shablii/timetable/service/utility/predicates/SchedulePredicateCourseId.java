package com.shablii.timetable.service.utility.predicates;

import com.shablii.timetable.constraints.IdValid;
import com.shablii.timetable.model.Schedule;
import lombok.*;

@EqualsAndHashCode
@RequiredArgsConstructor
public class SchedulePredicateCourseId implements SchedulePredicate {

    @IdValid("Course")
    private final long id;

    @Override
    public boolean test(Schedule schedule) {

        return schedule.getCourse().getId().equals(id);
    }

    @Override
    public String getCriteria() {

        return "course: " + id;
    }

}
