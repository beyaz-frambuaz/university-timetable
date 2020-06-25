package com.foxminded.timetable.dao.jpa;

import com.foxminded.timetable.dao.DataEraseDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
@Slf4j
public class DataEraseDaoImpl implements DataEraseDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void eraseAllData() {

        String sql = "DELETE FROM rescheduling_options; DELETE FROM schedules;"
                + "DELETE FROM schedule_templates;"
                + "DELETE FROM professors_courses; DELETE FROM auditoriums;"
                + "DELETE FROM courses; DELETE FROM groups;"
                + "DELETE FROM professors; DELETE FROM students;";
        entityManager.createNativeQuery(sql).executeUpdate();
        log.info("Erased all university and timetable data");
    }

    @Override
    public void resetSequences() {

        String sql = "alter sequence auditorium_id_seq restart with 1;"
                + "alter sequence course_id_seq restart with 1;"
                + "alter sequence group_id_seq restart with 1;"
                + "alter sequence professor_id_seq restart with 1;"
                + "alter sequence student_id_seq restart with 1;"
                + "alter sequence schedule_template_id_seq restart with 1;"
                + "alter sequence schedule_id_seq restart with 1;"
                + "alter sequence rescheduling_option_id_seq restart with 1;";
        entityManager.createNativeQuery(sql).executeUpdate();
        log.info("Reset all id generation sequences");
    }

    @Override
    public void eraseTimetableData() {

        String sql = "DELETE FROM rescheduling_options; DELETE FROM schedules;"
                + "DELETE FROM schedule_templates;";
        entityManager.createNativeQuery(sql).executeUpdate();
        log.info("Erased all timetable data");
    }

}
