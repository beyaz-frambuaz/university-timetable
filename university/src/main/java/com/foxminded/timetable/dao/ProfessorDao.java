package com.foxminded.timetable.dao;

import com.foxminded.timetable.model.Period;
import com.foxminded.timetable.model.Professor;

import java.time.LocalDate;
import java.util.List;

public interface ProfessorDao extends GenericDao<Professor> {

    void saveAllProfessorsCourses(List<Professor> professors);

    List<Professor> findAllAvailable(boolean weekParity, LocalDate date,
            Period period);

    Professor save(Professor professor);

    Professor update(Professor professor);

}
