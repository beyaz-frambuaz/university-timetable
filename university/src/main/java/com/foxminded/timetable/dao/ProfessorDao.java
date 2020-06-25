package com.foxminded.timetable.dao;

import com.foxminded.timetable.model.Period;
import com.foxminded.timetable.model.Professor;

import java.time.LocalDate;
import java.util.List;

public interface ProfessorDao extends GenericDao<Professor> {

    List<Professor> findAllAvailable(LocalDate date, Period period);

}
