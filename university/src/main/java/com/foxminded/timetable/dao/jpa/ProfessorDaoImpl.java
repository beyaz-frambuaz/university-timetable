package com.foxminded.timetable.dao.jpa;

import com.foxminded.timetable.dao.ProfessorDao;
import com.foxminded.timetable.model.Period;
import com.foxminded.timetable.model.Professor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.List;

@Repository
@Slf4j
public class ProfessorDaoImpl extends GenericDaoImpl<Professor>
        implements ProfessorDao {

    public ProfessorDaoImpl() {

        super(Professor.class);
    }

    @Override
    public List<Professor> findAllAvailable(LocalDate date, Period period) {

        log.debug("Retrieving available professors for {} on {}", period, date);

        TypedQuery<Professor> query = this.entityManager.createQuery(
                "select p from Professor p where p not in "
                        + "(select s.professor from Schedule s where"
                        + " s.date = :date and s.period = :period)",
                this.entityClass)
                .setParameter("date", date)
                .setParameter("period", period);

        return query.getResultList();
    }

}
