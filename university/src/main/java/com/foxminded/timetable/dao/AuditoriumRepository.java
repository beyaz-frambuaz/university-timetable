package com.foxminded.timetable.dao;

import com.foxminded.timetable.model.Auditorium;
import com.foxminded.timetable.model.Period;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AuditoriumRepository extends JpaRepository<Auditorium, Long> {

    @Query("select a from Auditorium a where a not in "
                   + "(select s.auditorium from Schedule s where"
                   + " s.date = :date and s.period = :period)")
    List<Auditorium> findAllAvailable(@Param("date") LocalDate date,
            @Param("period") Period period);

}
