package com.shablii.timetable.service;

import com.shablii.timetable.dao.ReschedulingOptionRepository;
import com.shablii.timetable.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ReschedulingOptionServiceTest {

    private final ReschedulingOption option = new ReschedulingOption(1L, DayOfWeek.MONDAY, Period.FIRST,
            new Auditorium("A-01"));
    @Mock
    private ReschedulingOptionRepository repository;
    @InjectMocks
    private ReschedulingOptionService service;

    @Test
    public void countShouldDelegateToRepository() {

        long expected = 1L;
        given(repository.count()).willReturn(expected);

        long actual = repository.count();

        then(repository).should().count();
        assertThat(actual).isEqualTo(expected);

    }

    @Test
    public void saveAllShouldDelegateToRepository() {

        List<ReschedulingOption> options = Collections.singletonList(option);
        given(repository.saveAll(anyList())).willReturn(options);

        List<ReschedulingOption> actual = service.saveAll(options);

        then(repository).should().saveAll(options);
        assertThat(actual).hasSameElementsAs(options);
    }

    @Test
    public void saveAllShouldReturnListBackGivenEmptyList() {

        List<ReschedulingOption> expected = Collections.emptyList();

        List<ReschedulingOption> actual = service.saveAll(expected);

        then(repository).shouldHaveNoInteractions();
        assertThat(actual).isEmpty();
    }

    @Test
    public void findAllShouldDelegateToRepository() {

        List<ReschedulingOption> options = Collections.singletonList(option);
        given(repository.findAll()).willReturn(options);

        List<ReschedulingOption> actual = service.findAll();

        then(repository).should().findAll();
        assertThat(actual).isEqualTo(options);
    }

    @Test
    public void findByIdShouldDelegateToRepository() {

        long id = 1L;
        Optional<ReschedulingOption> expected = Optional.of(option);
        given(repository.findById(anyLong())).willReturn(expected);

        Optional<ReschedulingOption> actual = service.findById(id);

        then(repository).should().findById(id);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void findAllForDayShouldDelegateToRepository() {

        List<ReschedulingOption> expected = Collections.singletonList(option);
        DayOfWeek day = DayOfWeek.MONDAY;
        given(repository.findAllByDay(any(DayOfWeek.class))).willReturn(expected);

        List<ReschedulingOption> actual = service.findAllForDay(day);

        then(repository).should().findAllByDay(day);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void deleteAllShouldDelegateToRepository() {

        service.deleteAll();

        then(repository).should().deleteAllInBatch();
    }

}
