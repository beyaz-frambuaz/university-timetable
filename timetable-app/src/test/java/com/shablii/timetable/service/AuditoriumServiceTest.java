package com.shablii.timetable.service;

import com.shablii.timetable.dao.AuditoriumRepository;
import com.shablii.timetable.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AuditoriumServiceTest {

    private final Auditorium auditorium = new Auditorium(1L, "A-01");
    @Mock
    private AuditoriumRepository repository;
    @InjectMocks
    private AuditoriumService service;

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

        Auditorium expected = new Auditorium("A-04");
        given(repository.save(any(Auditorium.class))).willReturn(expected);

        Auditorium actual = service.save(expected);

        then(repository).should().save(expected);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void saveAllShouldDelegateToRepository() {

        List<Auditorium> auditoriums = Collections.singletonList(auditorium);
        given(repository.saveAll(anyList())).willReturn(auditoriums);

        List<Auditorium> actual = service.saveAll(auditoriums);

        then(repository).should().saveAll(auditoriums);
        assertThat(actual).hasSameElementsAs(auditoriums);
    }

    @Test
    public void saveAllShouldReturnListBackGivenEmptyList() {

        List<Auditorium> expected = Collections.emptyList();

        List<Auditorium> actual = service.saveAll(expected);

        then(repository).shouldHaveNoInteractions();
        assertThat(actual).isEmpty();
    }

    @Test
    public void findAllShouldDelegateToRepository() {

        List<Auditorium> auditoriums = Collections.singletonList(auditorium);
        given(repository.findAll()).willReturn(auditoriums);

        List<Auditorium> actual = service.findAll();

        then(repository).should().findAll();
        assertThat(actual).isEqualTo(auditoriums);
    }

    @Test
    public void findByIdShouldDelegateToRepository() {

        long id = 1L;
        Optional<Auditorium> expected = Optional.of(auditorium);
        given(repository.findById(anyLong())).willReturn(expected);

        Optional<Auditorium> actual = service.findById(id);

        then(repository).should().findById(id);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void findAvailableForShouldDelegateToRepository() {

        List<Auditorium> auditoriums = Collections.singletonList(auditorium);
        LocalDate date = LocalDate.MAX;
        Period period = Period.FIRST;
        given(repository.findAllAvailable(any(LocalDate.class), any(Period.class))).willReturn(auditoriums);

        List<Auditorium> actual = service.findAvailableFor(date, period);

        then(repository).should().findAllAvailable(date, period);
        assertThat(actual).isEqualTo(auditoriums);
    }

    @Test
    public void deleteShouldDelegateToRepository() {

        service.delete(auditorium);

        then(repository).should().delete(auditorium);
    }

    @Test
    public void deleteAllShouldDelegateToRepository() {

        service.deleteAll();

        then(repository).should().deleteAllInBatch();
    }

}
