package com.foxminded.timetable.dao;

import com.foxminded.timetable.model.Group;
import com.foxminded.timetable.model.Student;

import java.util.List;

public interface StudentDao extends GenericDao<Student> {

    List<Student> findAllInGroups(List<Group> groups);

}
