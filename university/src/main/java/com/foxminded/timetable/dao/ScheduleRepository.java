package com.foxminded.timetable.dao;

import com.foxminded.timetable.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findAllByDate(LocalDate date);

    List<Schedule> findAllByTemplateId(long templateId);

    List<Schedule> findAllByDateBetween(LocalDate startDate, LocalDate endDate);

}
