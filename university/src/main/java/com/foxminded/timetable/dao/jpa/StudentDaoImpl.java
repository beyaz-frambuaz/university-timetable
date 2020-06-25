package com.foxminded.timetable.dao.jpa;

import com.foxminded.timetable.dao.StudentDao;
import com.foxminded.timetable.model.Group;
import com.foxminded.timetable.model.Student;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class StudentDaoImpl extends GenericDaoImpl<Student>
        implements StudentDao {

    public StudentDaoImpl() {

        super(Student.class);
    }

    @Override
    public List<Student> findAllInGroups(List<Group> groups) {

        List<Long> groupIds =
                groups.stream().map(Group::getId).collect(Collectors.toList());

        if (groupIds.isEmpty()) {
            log.debug("Got empty list, returning empty result");
            return Collections.emptyList();
        }

        log.debug("Retrieving students in {}", groupIds);

        String jpql = "select s from Student s where s.group.id in :groupIds";

        return this.entityManager.createQuery(jpql, this.entityClass)
                .setParameter("groupIds", groupIds)
                .getResultList();
    }

}
