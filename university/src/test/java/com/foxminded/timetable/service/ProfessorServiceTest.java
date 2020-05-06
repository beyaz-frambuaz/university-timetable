package com.foxminded.timetable.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.foxminded.timetable.dao.ProfessorDao;
import com.foxminded.timetable.model.Course;
import com.foxminded.timetable.model.Period;
import com.foxminded.timetable.model.Professor;

@ExtendWith(MockitoExtension.class)
class ProfessorServiceTest {

    @Mock
    private ProfessorDao repository;

    @InjectMocks
    private ProfessorService service;

    private Professor professor = new Professor(1L, "one", "one");

    @Test
    public void countShouldDelegateToRepository() {

        long expected = 1L;
        given(repository.count()).willReturn(expected);

        long actual = repository.count();

        then(repository).should().count();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void saveShouldAddProfessorToRepositoryIfNewAndNotAddCoursesIfMissing() {

        Professor expected = new Professor("new", "new");
        given(repository.save(any(Professor.class))).willReturn(expected);

        Professor actual = service.save(expected);

        then(repository).should().save(expected);
        then(repository).shouldHaveNoMoreInteractions();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void saveShouldAddProfessorToRepositoryIfNewAndAddCourses() {

        Course course = new Course(1L, "course");
        Professor expected = new Professor("new", "new");
        expected.addCourse(course);
        given(repository.save(any(Professor.class))).willReturn(expected);

        Professor actual = service.save(expected);

        then(repository).should().save(expected);
        then(repository).should()
                .saveAllProfessorsCourses(Arrays.asList(expected));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void saveShouldUpdateProfessorInRepositoryIfExistingWithCourses() {

        Course course = new Course(1L, "course");
        Professor expected = new Professor(1L, "one", "one");
        expected.addCourse(course);
        given(repository.update(any(Professor.class))).willReturn(expected);

        Professor actual = service.save(expected);

        then(repository).should().update(expected);
        then(repository).shouldHaveNoMoreInteractions();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void saveShouldReturnWithoutUpdateIfExistingWithNoCourses() {

        Professor actual = service.save(professor);

        then(repository).shouldHaveNoMoreInteractions();
        assertThat(actual).isEqualTo(professor);
    }

    @Test
    public void saveAllShouldDelegateToRepository() {

        List<Professor> professors = Arrays.asList(professor);
        given(repository.saveAll(anyList())).willReturn(professors);

        List<Professor> actual = service.saveAll(professors);

        then(repository).should().saveAll(professors);
        assertThat(actual).hasSameElementsAs(professors);
    }

    @Test
    public void saveAllShouldReturnListBackGivenEmptyList() {

        List<Professor> expected = Collections.emptyList();

        List<Professor> actual = service.saveAll(expected);

        then(repository).shouldHaveNoInteractions();
        assertThat(actual).isEmpty();
    }

    @Test
    public void findAllShouldDelegateToRepository() {

        List<Professor> professors = Arrays.asList(professor);
        given(repository.findAll()).willReturn(professors);

        List<Professor> actual = service.findAll();

        then(repository).should().findAll();
        assertThat(actual).isEqualTo(professors);
    }

    @Test
    public void findByIdShouldDelegateToRepository() {

        long id = 1L;
        given(repository.findById(anyLong()))
                .willReturn(Optional.of(professor));

        Optional<Professor> actual = service.findById(id);

        then(repository).should().findById(id);
        assertThat(actual).isPresent().contains(professor);
    }

    @Test
    public void findAvailableForShouldDelegateToRepository() {

        List<Professor> professors = Arrays.asList(professor);
        boolean weekParity = false;
        LocalDate date = LocalDate.MAX;
        Period period = Period.FIRST;
        given(repository.findAllAvailable(anyBoolean(), any(LocalDate.class),
                any(Period.class))).willReturn(professors);

        List<Professor> actual = service.findAvailableFor(weekParity, date,
                period);

        then(repository).should().findAllAvailable(weekParity, date, period);
        assertThat(actual).isEqualTo(professors);
    }

}
