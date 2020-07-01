package com.foxminded.timetable.service;

import com.foxminded.timetable.dao.ProfessorRepository;
import com.foxminded.timetable.model.Period;
import com.foxminded.timetable.model.Professor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ProfessorServiceTest {

    private final Professor professor = new Professor(1L, "one", "one");
    @Mock
    private ProfessorRepository repository;
    @InjectMocks
    private ProfessorService service;

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

        Professor expected = new Professor("new", "new");
        given(repository.save(any(Professor.class))).willReturn(expected);

        Professor actual = service.save(expected);

        then(repository).should().save(expected);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void saveAllShouldDelegateToRepository() {

        List<Professor> professors = Collections.singletonList(professor);
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

        List<Professor> professors = Collections.singletonList(professor);
        given(repository.findAll()).willReturn(professors);

        List<Professor> actual = service.findAll();

        then(repository).should().findAll();
        assertThat(actual).isEqualTo(professors);
    }

    @Test
    public void findByIdShouldDelegateToRepository() {

        long id = 1L;
        Optional<Professor> expected = Optional.of(professor);
        given(repository.findById(anyLong())).willReturn(expected);

        Optional<Professor> actual = service.findById(id);

        then(repository).should().findById(id);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void findAvailableForShouldDelegateToRepository() {

        List<Professor> professors = Collections.singletonList(professor);
        LocalDate date = LocalDate.MAX;
        Period period = Period.FIRST;
        given(repository.findAllAvailable(any(LocalDate.class),
                any(Period.class))).willReturn(professors);

        List<Professor> actual = service.findAvailableFor(date, period);

        then(repository).should().findAllAvailable(date, period);
        assertThat(actual).isEqualTo(professors);
    }

    @Test
    public void deleteShouldDelegateToRepository() {

        service.delete(professor);

        then(repository).should().delete(professor);
    }

    @Test
    public void deleteAllShouldDelegateToRepository() {

        service.deleteAll();

        then(repository).should().deleteAllInBatch();
    }

}
