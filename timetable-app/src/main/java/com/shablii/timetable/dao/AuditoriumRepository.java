package com.shablii.timetable.dao;

import com.shablii.timetable.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AuditoriumRepository extends JpaRepository<Auditorium, Long> {

    @Query("select a from Auditorium a where a not in " + "(select s.auditorium from Schedule s where"
                   + " s.date = :date and s.period = :period)")
    List<Auditorium> findAllAvailable(@Param("date") LocalDate date, @Param("period") Period period);

}
