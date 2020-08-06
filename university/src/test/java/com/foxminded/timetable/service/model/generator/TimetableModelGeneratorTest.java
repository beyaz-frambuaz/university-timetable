package com.foxminded.timetable.service.model.generator;

import com.foxminded.timetable.model.*;
import com.foxminded.timetable.service.TimetableFacade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.MethodMode;

import java.time.DayOfWeek;
import java.util.*;

import static java.util.stream.Collectors.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.atLeastOnce;

@SpringBootTest
class TimetableModelGeneratorTest {

    @SpyBean
    private TimetableFacade timetableFacade;

    @Autowired
    private TimetableModelGenerator timetableModelGenerator;

    @Test
    @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
    public void generateAndSaveShouldSaveGeneratedDataToRepository() {

        timetableModelGenerator.generateAndSave();

        then(timetableFacade).should(atLeastOnce()).saveOptions(anyList());
        then(timetableFacade).should(atLeastOnce()).saveTemplates(anyList());
    }

    @Test
    public void repositoryShouldHaveHundredTwentyFiveReschedulingOptions() {

        long expected = 125;

        long actual = timetableFacade.countOptions();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void repositoryShouldHaveTwentyFiveReschedulingOptionsPerDay() {

        Map<DayOfWeek, Long> daysOptions = timetableFacade.getOptions()
                .stream()
                .collect(groupingBy(ReschedulingOption::getDay, counting()));

        assertThat(daysOptions.values()).containsOnly(25L);
    }

    @Test
    public void repositoryShouldHaveFiveReschedulingOptionsPerPeriod() {

        List<Long> optionsPerDayPerPeriod = timetableFacade.getOptions()
                .stream()
                .collect(groupingBy(ReschedulingOption::getDay,
                        groupingBy(ReschedulingOption::getPeriod, counting())))
                .values()
                .stream()
                .flatMap(periodOptions -> periodOptions.values().stream())
                .collect(toList());

        assertThat(optionsPerDayPerPeriod).containsOnly(5L);
    }

    @Test
    public void repositoryShouldHaveReschedulingOptionsForWorkDaysOnly() {

        List<ReschedulingOption> actual = timetableFacade.getOptions();

        assertThat(actual).noneMatch(
                option -> option.getDay() == DayOfWeek.SATURDAY
                        || option.getDay() == DayOfWeek.SUNDAY);
    }

    @Test
    public void scheduleTemplatesShouldContainAllGroups() {

        List<Group> expected = timetableFacade.getGroups();

        List<Group> actual = timetableFacade.getTwoWeekSchedule()
                .stream()
                .map(ScheduleTemplate::getGroup)
                .distinct()
                .collect(toList());

        assertThat(actual).hasSameElementsAs(expected);
    }

    @Test
    public void scheduleTemplatesShouldContainAllCourses() {

        List<Course> expected = timetableFacade.getCourses();

        List<Course> actual = timetableFacade.getTwoWeekSchedule()
                .stream()
                .map(ScheduleTemplate::getCourse)
                .distinct()
                .collect(toList());

        assertThat(actual).hasSameElementsAs(expected);
    }

    @Test
    public void scheduleTemplatesShouldContainAllCoursesForEachGroup() {

        List<Course> expected = timetableFacade.getCourses();

        Map<Group, List<Course>> eachGroupCourses =
                timetableFacade.getTwoWeekSchedule()
                        .stream()
                        .collect(groupingBy(ScheduleTemplate::getGroup,
                                mapping(ScheduleTemplate::getCourse,
                                        toList())));

        for (List<Course> groupCourses : eachGroupCourses.values()) {
            assertThat(groupCourses).hasSameElementsAs(expected);
        }
    }

    @Test
    public void scheduleTemplatesShouldContainAllProfessors() {

        List<Professor> expected = timetableFacade.getProfessors();

        List<Professor> actual = timetableFacade.getTwoWeekSchedule()
                .stream()
                .map(ScheduleTemplate::getProfessor)
                .distinct()
                .collect(toList());

        assertThat(actual).usingElementComparatorIgnoringFields("courses")
                .hasSameElementsAs(expected);
    }

    @Test
    public void scheduleTemplatesShouldHaveNoAuditoriumCollisions() {

        List<ScheduleTemplate> templates = timetableFacade.getTwoWeekSchedule();

        List<DayOfWeek> workDays =
                Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
                        DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
                        DayOfWeek.FRIDAY);

        for (boolean weekParity : Arrays.asList(Boolean.FALSE, Boolean.TRUE)) {
            for (DayOfWeek day : workDays) {
                for (Period period : Period.values()) {

                    assertThat(templates).filteredOn(
                            template -> template.getWeekParity() == weekParity
                                    && template.getDay() == day
                                    && template.getPeriod() == period)
                            .extracting(ScheduleTemplate::getAuditorium)
                            .doesNotHaveDuplicates();
                }
            }
        }
    }

    @Test
    public void scheduleTemplatesShouldHaveNoProfessorCollisions() {

        List<ScheduleTemplate> templates = timetableFacade.getTwoWeekSchedule();

        List<DayOfWeek> workDays =
                Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
                        DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
                        DayOfWeek.FRIDAY);

        for (boolean weekParity : Arrays.asList(Boolean.FALSE, Boolean.TRUE)) {
            for (DayOfWeek day : workDays) {
                for (Period period : Period.values()) {

                    assertThat(templates).filteredOn(
                            template -> template.getWeekParity() == weekParity
                                    && template.getDay() == day
                                    && template.getPeriod() == period)
                            .extracting(ScheduleTemplate::getProfessor)
                            .doesNotHaveDuplicates();
                }
            }
        }
    }

    @Test
    public void scheduleTemplatesShouldHaveNoGroupCollisions() {

        List<ScheduleTemplate> templates = timetableFacade.getTwoWeekSchedule();

        List<DayOfWeek> workDays =
                Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
                        DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
                        DayOfWeek.FRIDAY);

        for (boolean weekParity : Arrays.asList(Boolean.FALSE, Boolean.TRUE)) {
            for (DayOfWeek day : workDays) {
                for (Period period : Period.values()) {

                    assertThat(templates).filteredOn(
                            template -> template.getWeekParity() == weekParity
                                    && template.getDay() == day
                                    && template.getPeriod() == period)
                            .extracting(ScheduleTemplate::getGroup)
                            .doesNotHaveDuplicates();
                }
            }
        }
    }

}
