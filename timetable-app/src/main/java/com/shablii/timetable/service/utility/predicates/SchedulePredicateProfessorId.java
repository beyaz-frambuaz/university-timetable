package com.shablii.timetable.service.utility.predicates;

import com.shablii.timetable.constraints.IdValid;
import com.shablii.timetable.model.Schedule;
import lombok.*;

@EqualsAndHashCode
@RequiredArgsConstructor
public class SchedulePredicateProfessorId implements SchedulePredicate {

    @IdValid("Professor")
    private final long id;

    @Override
    public boolean test(Schedule schedule) {

        return schedule.getProfessor().getId().equals(id);
    }

    @Override
    public String getCriteria() {

        return "professor: " + id;
    }

}
