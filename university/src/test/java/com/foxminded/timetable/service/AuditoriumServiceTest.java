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

import com.foxminded.timetable.dao.AuditoriumDao;
import com.foxminded.timetable.model.Auditorium;
import com.foxminded.timetable.model.Period;

@ExtendWith(MockitoExtension.class)
class AuditoriumServiceTest {

    @Mock
    private AuditoriumDao repository;

    @InjectMocks
    private AuditoriumService service;

    private Auditorium auditorium = new Auditorium(1L, "A-01");

    @Test
    public void countShouldDelegateToRepository() {

        long expected = 1L;
        given(repository.count()).willReturn(expected);

        long actual = repository.count();

        then(repository).should().count();
        assertThat(actual).isEqualTo(expected);

    }

    @Test
    public void saveShouldAddAuditoriumToRepositoryIfNew() {

        Auditorium expected = new Auditorium("A-04");
        given(repository.save(any(Auditorium.class))).willReturn(expected);

        Auditorium actual = service.save(expected);

        then(repository).should().save(expected);
        then(repository).shouldHaveNoMoreInteractions();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void saveShouldUpdateAuditoriumInRepositoryIfExisting() {

        Auditorium expected = new Auditorium(4L, "A-04");
        given(repository.update(any(Auditorium.class))).willReturn(expected);

        Auditorium actual = service.save(expected);

        then(repository).should().update(expected);
        then(repository).shouldHaveNoMoreInteractions();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void saveAllShouldDelegateToRepository() {

        List<Auditorium> auditoriums = Arrays.asList(auditorium);
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

        List<Auditorium> auditoriums = Arrays.asList(auditorium);
        given(repository.findAll()).willReturn(auditoriums);

        List<Auditorium> actual = service.findAll();

        then(repository).should().findAll();
        assertThat(actual).isEqualTo(auditoriums);
    }

    @Test
    public void findByIdShouldDelegateToRepository() {

        long id = 1L;
        given(repository.findById(anyLong()))
                .willReturn(Optional.of(auditorium));

        Optional<Auditorium> actual = service.findById(id);

        then(repository).should().findById(id);
        assertThat(actual).isPresent().contains(auditorium);
    }

    @Test
    public void findAvailableForShouldDelegateToRepository() {

        List<Auditorium> auditoriums = Arrays.asList(auditorium);
        boolean weekParity = false;
        LocalDate date = LocalDate.MAX;
        Period period = Period.FIRST;
        given(repository.findAllAvailable(anyBoolean(), any(LocalDate.class),
                any(Period.class))).willReturn(auditoriums);

        List<Auditorium> actual = service.findAvailableFor(weekParity, date,
                period);

        then(repository).should().findAllAvailable(weekParity, date, period);
        assertThat(actual).isEqualTo(auditoriums);
    }

}
