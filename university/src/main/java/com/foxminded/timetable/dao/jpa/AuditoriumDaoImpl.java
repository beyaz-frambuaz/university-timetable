package com.foxminded.timetable.dao.jpa;

import com.foxminded.timetable.dao.AuditoriumDao;
import com.foxminded.timetable.model.Auditorium;
import com.foxminded.timetable.model.Period;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.List;

@Repository
@Slf4j
public class AuditoriumDaoImpl extends GenericDaoImpl<Auditorium>
        implements AuditoriumDao {

    public AuditoriumDaoImpl() {

        super(Auditorium.class);
    }

    @Override
    public List<Auditorium> findAllAvailable(LocalDate date, Period period) {

        log.debug("Retrieving available auditoriums for {} on {}", period,
                date);

        TypedQuery<Auditorium> query = this.entityManager.createQuery(
                "select a from Auditorium a where a not in "
                        + "(select s.auditorium from Schedule s where"
                        + " s.date = :date and s.period = :period)",
                this.entityClass)
                .setParameter("date", date)
                .setParameter("period", period);

        return query.getResultList();
    }

}
