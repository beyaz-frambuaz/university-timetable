package com.foxminded.timetable.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.foxminded.timetable.dao.CourseDao;
import com.foxminded.timetable.model.Course;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseDao repository;

    @InjectMocks
    private CourseService service;

    private Course course = new Course(1L, "A-01");

    @Test
    public void countShouldDelegateToRepository() {

        long expected = 1L;
        given(repository.count()).willReturn(expected);

        long actual = repository.count();

        then(repository).should().count();
        assertThat(actual).isEqualTo(expected);

    }

    @Test
    public void saveShouldAddCourseToRepositoryIfNew() {

        Course expected = new Course("A-04");
        given(repository.save(any(Course.class))).willReturn(expected);

        Course actual = service.save(expected);

        then(repository).should().save(expected);
        then(repository).shouldHaveNoMoreInteractions();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void saveShouldUpdateCourseInRepositoryIfExisting() {

        Course expected = new Course(4L, "A-04");
        given(repository.update(any(Course.class))).willReturn(expected);

        Course actual = service.save(expected);

        then(repository).should().update(expected);
        then(repository).shouldHaveNoMoreInteractions();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void saveAllShouldDelegateToRepository() {

        List<Course> courses = Arrays.asList(course);
        given(repository.saveAll(anyList())).willReturn(courses);

        List<Course> actual = service.saveAll(courses);

        then(repository).should().saveAll(courses);
        assertThat(actual).hasSameElementsAs(courses);
    }

    @Test
    public void saveAllShouldReturnListBackGivenEmptyList() {

        List<Course> expected = Collections.emptyList();

        List<Course> actual = service.saveAll(expected);

        then(repository).shouldHaveNoInteractions();
        assertThat(actual).isEmpty();
    }

    @Test
    public void findAllShouldDelegateToRepository() {

        List<Course> courses = Arrays.asList(course);
        given(repository.findAll()).willReturn(courses);

        List<Course> actual = service.findAll();

        then(repository).should().findAll();
        assertThat(actual).isEqualTo(courses);
    }

    @Test
    public void findByIdShouldDelegateToRepository() {

        long id = 1L;
        given(repository.findById(anyLong()))
                .willReturn(Optional.of(course));

        Optional<Course> actual = service.findById(id);

        then(repository).should().findById(id);
        assertThat(actual).isPresent().contains(course);
    }

}
