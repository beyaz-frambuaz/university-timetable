package com.foxminded.timetable.dao.jpa;

import com.foxminded.timetable.dao.ReschedulingOptionDao;
import com.foxminded.timetable.model.ReschedulingOption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import java.time.DayOfWeek;
import java.util.List;

@Repository
@Slf4j
public class ReschedulingOptionDaoImpl
        extends GenericDaoImpl<ReschedulingOption>
        implements ReschedulingOptionDao {

    public ReschedulingOptionDaoImpl() {

        super(ReschedulingOption.class);
    }

    @Override
    public List<ReschedulingOption> findAllByDay(DayOfWeek dayOfWeek) {

        log.debug("Retrieving options for {}", dayOfWeek);

        String jpql = "select o from ReschedulingOption o "
                + "where o.day = :dayOfWeek";
        TypedQuery<ReschedulingOption> query =
                this.entityManager.createQuery(jpql, this.entityClass)
                        .setParameter("dayOfWeek", dayOfWeek);

        return query.getResultList();
    }

}
