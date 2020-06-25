package com.foxminded.timetable.dao.jpa;

import com.foxminded.timetable.dao.ScheduleTemplateDao;
import com.foxminded.timetable.model.ScheduleTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import java.time.DayOfWeek;
import java.util.List;

@Repository
@Slf4j
public class ScheduleTemplateDaoImpl extends GenericDaoImpl<ScheduleTemplate>
        implements ScheduleTemplateDao {

    public ScheduleTemplateDaoImpl() {

        super(ScheduleTemplate.class);
    }

    @Override
    public List<ScheduleTemplate> findAllByWeek(boolean weekParity) {

        log.debug("Retrieving schedule templates for week parity {}",
                weekParity);

        String jpql = "select t from ScheduleTemplate t "
                + "where t.weekParity = :weekParity";
        TypedQuery<ScheduleTemplate> query =
                this.entityManager.createQuery(jpql, this.entityClass)
                        .setParameter("weekParity", weekParity);

        return query.getResultList();
    }

    @Override
    public List<ScheduleTemplate> findAllByDay(boolean weekParity,
            DayOfWeek day) {

        log.debug("Retrieving schedule templates for week parity {} on {}",
                weekParity, day);

        String jpql = "select t from ScheduleTemplate t "
                + "where t.weekParity = :weekParity and t.day = :day";
        TypedQuery<ScheduleTemplate> query =
                this.entityManager.createQuery(jpql, this.entityClass)
                        .setParameter("weekParity", weekParity)
                        .setParameter("day", day);

        return query.getResultList();
    }

}
