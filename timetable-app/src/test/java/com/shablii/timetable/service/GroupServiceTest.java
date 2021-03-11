package com.shablii.timetable.service;

import com.shablii.timetable.dao.GroupRepository;
import com.shablii.timetable.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    private final Group group = new Group(1L, "A-01");
    @Mock
    private GroupRepository repository;
    @InjectMocks
    private GroupService service;

    @Test
    public void countShouldDelegateToRepository() {

        long expected = 1L;
        given(repository.count()).willReturn(expected);

        long actual = repository.count();

        then(repository).should().count();
        assertThat(actual).isEqualTo(expected);

    }

    @Test
    public void saveShouldDelegateToRepository() {

        Group expected = new Group("A-04");
        given(repository.save(any(Group.class))).willReturn(expected);

        Group actual = service.save(expected);

        then(repository).should().save(expected);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void saveAllShouldDelegateToRepository() {

        List<Group> groups = Collections.singletonList(group);
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

        List<Group> groups = Collections.singletonList(group);
        given(repository.findAll()).willReturn(groups);

        List<Group> actual = service.findAll();

        then(repository).should().findAll();
        assertThat(actual).isEqualTo(groups);
    }

    @Test
    public void findAllAttendingProfessorCourseShouldDelegateToRepository() {

        List<Group> groups = Collections.singletonList(group);
        long professorId = 1L;
        Professor professor = mock(Professor.class);
        given(professor.getId()).willReturn(professorId);
        long courseId = 1L;
        Course course = mock(Course.class);
        given(course.getId()).willReturn(courseId);
        given(repository.findAllByProfessorAndCourse(anyLong(), anyLong())).willReturn(groups);

        List<Group> actual = service.findAllAttendingProfessorCourse(course, professor);

        then(repository).should().findAllByProfessorAndCourse(professorId, courseId);
        assertThat(actual).isEqualTo(groups);
    }

    @Test
    public void findByIdShouldDelegateToRepository() {

        long id = 1L;
        Optional<Group> expected = Optional.of(group);
        given(repository.findById(anyLong())).willReturn(expected);

        Optional<Group> actual = service.findById(id);

        then(repository).should().findById(id);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void deleteShouldDelegateToRepository() {

        service.delete(group);

        then(repository).should().delete(group);
    }

    @Test
    public void deleteAllShouldDelegateToRepository() {

        service.deleteAll();

        then(repository).should().deleteAllInBatch();
    }

}
