package com.foxminded.timetable.service;

import com.foxminded.timetable.dao.ScheduleTemplateDao;
import com.foxminded.timetable.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ScheduleTemplateServiceTest {

    private final Long                    id         = 1L;
    private final boolean                 weekParity = false;
    private final DayOfWeek               day        = DayOfWeek.MONDAY;
    private final Period                  period     = Period.FIRST;
    private final Auditorium              auditorium = new Auditorium(1L,
            "A-01");
    private final Course                  course     = new Course(1L, "course");
    private final Group                   group      = new Group(1L, "G-01");
    private final Professor               professor  = new Professor(1L, "one",
            "one");
    private final ScheduleTemplate        template   = new ScheduleTemplate(id,
            weekParity, day, period, auditorium, course, group, professor);
    @Mock
    private       ScheduleTemplateDao     repository;
    @InjectMocks
    private       ScheduleTemplateService service;

    @Test
    public void countShouldDelegateToRepository() {

        long expected = 1L;
        given(repository.count()).willReturn(expected);

        long actual = repository.count();

        then(repository).should().count();
        assertThat(actual).isEqualTo(expected);

    }

    @Test
    public void saveShouldAddScheduleTemplateToRepositoryIfNew() {

        ScheduleTemplate expected = new ScheduleTemplate(weekParity, day,
                period, auditorium, course, group, professor);
        given(repository.save(any(ScheduleTemplate.class))).willReturn(
                expected);

        ScheduleTemplate actual = service.save(expected);

        then(repository).should().save(expected);
        then(repository).shouldHaveNoMoreInteractions();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void saveShouldUpdateScheduleTemplateInRepositoryIfExisting() {

        given(repository.update(any(ScheduleTemplate.class))).willReturn(
                template);

        ScheduleTemplate actual = service.save(template);

        then(repository).should().update(template);
        then(repository).shouldHaveNoMoreInteractions();
        assertThat(actual).isEqualTo(template);
    }

    @Test
    public void saveAllShouldDelegateToRepository() {

        List<ScheduleTemplate> templates = Collections.singletonList(template);
        given(repository.saveAll(anyList())).willReturn(templates);

        List<ScheduleTemplate> actual = service.saveAll(templates);

        then(repository).should().saveAll(templates);
        assertThat(actual).hasSameElementsAs(templates);
    }

    @Test
    public void saveAllShouldReturnListBackGivenEmptyList() {

        List<ScheduleTemplate> expected = Collections.emptyList();

        List<ScheduleTemplate> actual = service.saveAll(expected);

        then(repository).shouldHaveNoInteractions();
        assertThat(actual).isEmpty();
    }

    @Test
    public void findAllShouldDelegateToRepository() {

        List<ScheduleTemplate> templates = Collections.singletonList(template);
        given(repository.findAll()).willReturn(templates);

        List<ScheduleTemplate> actual = service.findAll();

        then(repository).should().findAll();
        assertThat(actual).isEqualTo(templates);
    }

//    @Test
//    public void findByIdShouldDelegateToRepository() {
//
//        long id = 1L;
//        given(repository.findById(anyLong())).willReturn(Optional.of(template));
//
//        Optional<ScheduleTemplate> actual = service.findById(id);
//
//        then(repository).should().findById(id);
//        assertThat(actual).isPresent().contains(template);
//    }

}
