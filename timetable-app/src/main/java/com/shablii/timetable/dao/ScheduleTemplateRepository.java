package com.shablii.timetable.dao;

import com.shablii.timetable.model.ScheduleTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;

public interface ScheduleTemplateRepository extends JpaRepository<ScheduleTemplate, Long> {

    List<ScheduleTemplate> findAllByWeekParityAndDay(boolean weekParity, DayOfWeek day);

    List<ScheduleTemplate> findAllByWeekParity(boolean weekParity);

}
