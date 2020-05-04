package com.foxminded.timetable.dao;

import java.util.List;

import com.foxminded.timetable.model.Group;

public interface GroupDao extends GenericDao<Group> {

    List<Group> findAllByProfessorAndCourse(long professorId, long courseId);

    Group save(Group group);

    Group update(Group group);

}
