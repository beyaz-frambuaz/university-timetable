package com.foxminded.timetable.dao;

import java.time.LocalDate;
import java.util.List;

import com.foxminded.timetable.model.ReschedulingOption;
import com.foxminded.timetable.model.Schedule;

public interface ScheduleDao extends GenericDao<Schedule> {

    List<Schedule> findAllByDate(LocalDate date);
    
    List<Schedule> findAllByTemplateId(long templateId);

    void substituteProfessor(long scheduleId, long professorId);
    
    void reschedule(Schedule candidate, LocalDate targetDate,
            ReschedulingOption targetOption);
    
    void updateAllWithTemplateId(long templateId,
            ReschedulingOption targetOption, int deltaDays);

}
