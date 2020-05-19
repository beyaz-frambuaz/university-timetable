package com.foxminded.timetable.dao;

import com.foxminded.timetable.model.Auditorium;
import com.foxminded.timetable.model.Period;

import java.time.LocalDate;
import java.util.List;

public interface AuditoriumDao extends GenericDao<Auditorium> {

    List<Auditorium> findAllAvailable(boolean weekParity, LocalDate date,
            Period period);

    Auditorium save(Auditorium newAuditorium);

    Auditorium update(Auditorium auditorium);

}
