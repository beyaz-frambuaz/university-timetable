package com.foxminded.timetable.dao;

import com.foxminded.timetable.model.ReschedulingOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;

public interface ReschedulingOptionRepository
        extends JpaRepository<ReschedulingOption, Long> {

    List<ReschedulingOption> findAllByDay(DayOfWeek dayOfWeek);

}
