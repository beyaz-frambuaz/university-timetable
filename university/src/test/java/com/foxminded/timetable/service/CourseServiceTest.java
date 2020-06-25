package com.foxminded.timetable.service;

import com.foxminded.timetable.dao.CourseDao;
import com.foxminded.timetable.model.Course;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    private final Course        course = new Course(1L, "A-01");
    @Mock
    private       CourseDao     repository;
    @InjectMocks
    private       CourseService service;

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

        Course expected = new Course("A-04");
        given(repository.save(any(Course.class))).willReturn(expected);

        Course actual = service.save(expected);

        then(repository).should().save(expected);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void saveAllShouldDelegateToRepository() {

        List<Course> courses = Collections.singletonList(course);
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

        List<Course> courses = Collections.singletonList(course);
        given(repository.findAll()).willReturn(courses);

        List<Course> actual = service.findAll();

        then(repository).should().findAll();
        assertThat(actual).isEqualTo(courses);
    }

    @Test
    public void findByIdShouldDelegateToRepository() {

        long id = 1L;
        Optional<Course> expected = Optional.of(course);
        given(repository.findById(anyLong())).willReturn(expected);

        Optional<Course> actual = service.findById(id);

        then(repository).should().findById(id);
        assertThat(actual).isEqualTo(expected);
    }

}
