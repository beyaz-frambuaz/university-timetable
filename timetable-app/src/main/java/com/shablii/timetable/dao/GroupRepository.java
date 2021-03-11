package com.shablii.timetable.dao;

import com.shablii.timetable.model.Group;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long> {

    @Query("select t.group from ScheduleTemplate t " + "where t.professor.id = :professorId "
                   + "and t.course.id = :courseId")
    List<Group> findAllByProfessorAndCourse(@Param("professorId") long professorId, @Param("courseId") long courseId);

}
