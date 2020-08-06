package com.foxminded.timetable.dao;

import com.foxminded.timetable.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ProfessorRepository extends JpaRepository<Professor, Long> {

    @Query("select p from Professor p where p not in "
                   + "(select s.professor from Schedule s where"
                   + " s.date = :date and s.period = :period)")
    List<Professor> findAllAvailable(@Param("date") LocalDate date,
            @Param("period") Period period);

    List<Professor> findAllByCourses(@Param("course") Course course);
}
