package com.foxminded.timetable.dao.jdbc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JdbcDataRefreshDao {

    private final JdbcTemplate jdbc;

    public void eraseTimetableData() {

        String sql = "DELETE FROM rescheduling_options;"
                + "DELETE FROM schedules;"
                + "DELETE FROM schedule_templates;";
        jdbc.execute(sql);
        log.info("Erased all timetable data");
    }

    public void eraseAllData() {

        String sql = "DELETE FROM rescheduling_options;"
                + "DELETE FROM schedules;"
                + "DELETE FROM schedule_templates;"
                + "DELETE FROM professors_courses;"
                + "DELETE FROM auditoriums;"
                + "DELETE FROM courses;"
                + "DELETE FROM groups;"
                + "DELETE FROM professors;"
                + "DELETE FROM students;";
        jdbc.execute(sql);
        log.info("Erased all university and timetable data");
    }

}
