package com.shablii.timetable.dao.jpa;

import com.shablii.timetable.dao.ResetSequencesDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

@Repository
@RequiredArgsConstructor
public class ResetSequencesDaoImpl implements ResetSequencesDao {

    private final EntityManager entityManager;

    @Override
    public void resetSequences() {

        String sql = "alter sequence auditorium_id_seq restart with 1;" + "alter sequence course_id_seq restart with 1;"
                + "alter sequence group_id_seq restart with 1;" + "alter sequence professor_id_seq restart with 1;"
                + "alter sequence student_id_seq restart with 1;"
                + "alter sequence schedule_template_id_seq restart with 1;"
                + "alter sequence schedule_id_seq restart with 1;"
                + "alter sequence rescheduling_option_id_seq restart with 1;";
        entityManager.createNativeQuery(sql).executeUpdate();
    }

}
