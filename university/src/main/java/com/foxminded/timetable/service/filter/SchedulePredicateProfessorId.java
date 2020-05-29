package com.foxminded.timetable.service.filter;

import com.foxminded.timetable.model.Schedule;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SchedulePredicateProfessorId implements SchedulePredicate {

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
