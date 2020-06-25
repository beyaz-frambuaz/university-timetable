package com.foxminded.timetable.dao.jpa;

import com.foxminded.timetable.dao.GroupDao;
import com.foxminded.timetable.model.Group;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import java.util.List;

@Repository
@Slf4j
public class GroupDaoImpl extends GenericDaoImpl<Group> implements GroupDao {

    public GroupDaoImpl() {

        super(Group.class);
    }

    @Override
    public List<Group> findAllByProfessorAndCourse(long professorId,
            long courseId) {

        log.debug("Retrieving groups by professor (ID {}) and course (ID {})",
                professorId, courseId);

        TypedQuery<Group> query = this.entityManager.createQuery(
                "select t.group from ScheduleTemplate t "
                        + "where t.professor.id = :professorId "
                        + "and t.course.id = :courseId", this.entityClass)
                .setParameter("professorId", professorId)
                .setParameter("courseId", courseId);

        return query.getResultList();
    }

}
