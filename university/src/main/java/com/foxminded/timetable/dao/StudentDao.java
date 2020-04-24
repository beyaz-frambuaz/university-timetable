package com.foxminded.timetable.dao;

import java.util.List;

import com.foxminded.timetable.model.Group;
import com.foxminded.timetable.model.Student;

public interface StudentDao extends GenericDao<Student> {

    List<Student> findAllByGroups(List<Group> groups);
    
}
