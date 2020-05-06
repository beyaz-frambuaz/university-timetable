package com.foxminded.timetable.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.foxminded.timetable.dao.GroupDao;
import com.foxminded.timetable.model.Course;
import com.foxminded.timetable.model.Group;
import com.foxminded.timetable.model.Professor;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private GroupDao repository;

    @InjectMocks
    private GroupService service;

    private Group group = new Group(1L, "A-01");

    @Test
    public void countShouldDelegateToRepository() {

        long expected = 1L;
        given(repository.count()).willReturn(expected);

        long actual = repository.count();

        then(repository).should().count();
        assertThat(actual).isEqualTo(expected);

    }

    @Test
    public void saveShouldAddGroupToRepositoryIfNew() {

        Group expected = new Group("A-04");
        given(repository.save(any(Group.class))).willReturn(expected);

        Group actual = service.save(expected);

        then(repository).should().save(expected);
        then(repository).shouldHaveNoMoreInteractions();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void saveShouldUpdateGroupInRepositoryIfExisting() {

        Group expected = new Group(4L, "A-04");
        given(repository.update(any(Group.class))).willReturn(expected);

        Group actual = service.save(expected);

        then(repository).should().update(expected);
        then(repository).shouldHaveNoMoreInteractions();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void saveAllShouldDelegateToRepository() {

        List<Group> groups = Arrays.asList(group);
        given(repository.saveAll(anyList())).willReturn(groups);

        List<Group> actual = service.saveAll(groups);

        then(repository).should().saveAll(groups);
        assertThat(actual).hasSameElementsAs(groups);
    }

    @Test
    public void saveAllShouldReturnListBackGivenEmptyList() {

        List<Group> expected = Collections.emptyList();

        List<Group> actual = service.saveAll(expected);

        then(repository).shouldHaveNoInteractions();
        assertThat(actual).isEmpty();
    }

    @Test
    public void findAllShouldDelegateToRepository() {

        List<Group> groups = Arrays.asList(group);
        given(repository.findAll()).willReturn(groups);

        List<Group> actual = service.findAll();

        then(repository).should().findAll();
        assertThat(actual).isEqualTo(groups);
    }

    @Test
    public void findAllAttendingProfessorCourseShouldDelegateToRepository() {

        List<Group> groups = Arrays.asList(group);
        Long professorId = 1L;
        Professor professor = mock(Professor.class);
        given(professor.getId()).willReturn(professorId);
        Long courseId = 1L;
        Course course = mock(Course.class);
        given(course.getId()).willReturn(courseId);
        given(repository.findAllByProfessorAndCourse(anyLong(), anyLong()))
                .willReturn(groups);

        List<Group> actual = service.findAllAttendingProfessorCourse(course,
                professor);

        then(repository).should().findAllByProfessorAndCourse(professorId,
                courseId);
        assertThat(actual).isEqualTo(groups);
    }

    @Test
    public void findByIdShouldDelegateToRepository() {

        long id = 1L;
        given(repository.findById(anyLong())).willReturn(Optional.of(group));

        Optional<Group> actual = service.findById(id);

        then(repository).should().findById(id);
        assertThat(actual).isPresent().contains(group);
    }

}
