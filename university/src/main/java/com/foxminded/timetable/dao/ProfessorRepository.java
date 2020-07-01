package com.foxminded.timetable.dao;

import com.foxminded.timetable.model.Period;
import com.foxminded.timetable.model.Professor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ProfessorRepository extends JpaRepository<Professor, Long> {

    @Query("select p from Professor p where p not in "
                   + "(select s.professor from Schedule s where"
                   + " s.date = :date and s.period = :period)")
    List<Professor> findAllAvailable(@Param("date") LocalDate date,
            @Param("period") Period period);

}
