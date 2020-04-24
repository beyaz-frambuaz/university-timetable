package com.foxminded.timetable.dao;

import java.time.LocalDate;
import java.util.List;

import com.foxminded.timetable.model.Period;
import com.foxminded.timetable.model.Professor;

public interface ProfessorDao extends GenericDao<Professor> {

    void saveAllProfessorsCourses(List<Professor> professors);

    List<Professor> findAllAvailable(boolean weekParity, LocalDate date,
            Period period);
    
}
