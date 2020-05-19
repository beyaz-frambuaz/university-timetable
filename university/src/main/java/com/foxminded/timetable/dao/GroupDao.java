package com.foxminded.timetable.dao;

import com.foxminded.timetable.model.Group;

import java.util.List;

public interface GroupDao extends GenericDao<Group> {

    List<Group> findAllByProfessorAndCourse(long professorId, long courseId);

    Group save(Group group);

    Group update(Group group);

}
