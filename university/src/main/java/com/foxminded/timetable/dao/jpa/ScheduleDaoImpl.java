package com.foxminded.timetable.dao.jpa;

import com.foxminded.timetable.dao.ScheduleDao;
import com.foxminded.timetable.model.Schedule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.util.List;

@Repository
@Slf4j
public class ScheduleDaoImpl extends GenericDaoImpl<Schedule>
        implements ScheduleDao {

    public ScheduleDaoImpl() {

        super(Schedule.class);
    }

    @Override
    public List<Schedule> findAllByDate(LocalDate date) {

        log.debug("Retrieving schedules for {}", date);

        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Schedule> query = builder.createQuery(this.entityClass);
        Root<Schedule> schedule = query.from(this.entityClass);
        query.select(schedule)
                .where(builder.equal(schedule.<LocalDate>get("date"), date));

        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public List<Schedule> findAllInRange(LocalDate startDate,
            LocalDate endDate) {

        log.debug("Retrieving schedules in range {}-{}", startDate, endDate);

        CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Schedule> query = builder.createQuery(this.entityClass);
        Root<Schedule> schedule = query.from(this.entityClass);
        query.select(schedule)
                .where(builder.between(schedule.get("date"), startDate,
                        endDate));

        return this.entityManager.createQuery(query).getResultList();
    }

    @Override
    public List<Schedule> findAllByTemplateId(long templateId) {

        log.debug("Retrieving schedules by template ID {}", templateId);

        String jpql = "select s from Schedule s "
                + "where s.template.id = :templateId";

        return this.entityManager.createQuery(jpql, this.entityClass)
                .setParameter("templateId", templateId)
                .getResultList();
    }

}
