package com.foxminded.timetable.service.model.generator;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.atLeastOnce;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.MethodMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.foxminded.timetable.TimetableApp;
import com.foxminded.timetable.dao.CourseDao;
import com.foxminded.timetable.dao.GroupDao;
import com.foxminded.timetable.dao.ProfessorDao;
import com.foxminded.timetable.dao.ReschedulingOptionDao;
import com.foxminded.timetable.dao.ScheduleTemplateDao;
import com.foxminded.timetable.model.Course;
import com.foxminded.timetable.model.Group;
import com.foxminded.timetable.model.Period;
import com.foxminded.timetable.model.Professor;
import com.foxminded.timetable.model.ReschedulingOption;
import com.foxminded.timetable.model.ScheduleTemplate;
import com.foxminded.timetable.service.model.generator.TimetableModelGenerator;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TimetableApp.class, initializers = ConfigFileApplicationContextInitializer.class)
class TimetableModelGeneratorTest {

    @SpyBean
    private ScheduleTemplateDao templateRepository;
    @SpyBean
    private ReschedulingOptionDao reschedulingOptionRepository;

    @Autowired
    private GroupDao groupRepository;
    @Autowired
    private CourseDao courseRepository;
    @Autowired
    private ProfessorDao professorRepository;
    @Autowired
    private TimetableModelGenerator timetableModelGenerator;

    @Test
    @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
    public void generateAndSaveShouldSaveGeneratedDataToRepository() {

        timetableModelGenerator.generateAndSave();

        then(templateRepository).should(atLeastOnce()).saveAll(anyList());
        then(reschedulingOptionRepository).should(atLeastOnce())
                .saveAll(anyList());
    }

    @Test
    public void repositoryShouldHaveHundredTwentyFiveReschedulingOptions() {

        long expected = 125;

        long actual = reschedulingOptionRepository.count();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void repositoryShouldHaveTwentyFiveReschedulingOptionsPerDay() {

        Map<DayOfWeek, Long> daysOptions = reschedulingOptionRepository
                .findAll().stream()
                .collect(groupingBy(ReschedulingOption::getDay, counting()));

        assertThat(daysOptions.values()).containsOnly(25L);
    }

    @Test
    public void repositoryShouldHaveFiveReschedulingOptionsPerPeriod() {

        List<Long> optionsPerDayPerPeriod = reschedulingOptionRepository
                .findAll().stream()
                .collect(groupingBy(ReschedulingOption::getDay,
                        groupingBy(ReschedulingOption::getPeriod, counting())))
                .values().stream()
                .flatMap(periodOptions -> periodOptions.values().stream())
                .collect(toList());

        assertThat(optionsPerDayPerPeriod).containsOnly(5L);
    }

    @Test
    public void repositoryShouldHadeReschedulingOptionsForWorkDaysOnly() {

        List<ReschedulingOption> actual = reschedulingOptionRepository
                .findAll();

        assertThat(actual)
                .noneMatch(option -> option.getDay() == DayOfWeek.SATURDAY
                        || option.getDay() == DayOfWeek.SUNDAY);
    }

    @Test
    public void scheduleTemplatesShouldContainAllGroups() {

        List<Group> expected = groupRepository.findAll();

        List<Group> actual = templateRepository.findAll().stream()
                .map(ScheduleTemplate::getGroup).distinct().collect(toList());

        assertThat(actual).hasSameElementsAs(expected);
    }

    @Test
    public void scheduleTemplatesShouldContainAllCourses() {

        List<Course> expected = courseRepository.findAll();

        List<Course> actual = templateRepository.findAll().stream()
                .map(ScheduleTemplate::getCourse).distinct().collect(toList());

        assertThat(actual).hasSameElementsAs(expected);
    }

    @Test
    public void scheduleTemplatesShouldContainAllCoursesForEachGroup() {

        List<Course> expected = courseRepository.findAll();

        Map<Group, List<Course>> eachGroupCourses = templateRepository.findAll()
                .stream().collect(groupingBy(ScheduleTemplate::getGroup,
                        mapping(ScheduleTemplate::getCourse, toList())));

        for (List<Course> groupCourses : eachGroupCourses.values()) {
            assertThat(groupCourses).hasSameElementsAs(expected);
        }
    }

    @Test
    public void scheduleTemplatesShouldContainAllProfessors() {

        List<Professor> expected = professorRepository.findAll();

        List<Professor> actual = templateRepository.findAll().stream()
                .map(ScheduleTemplate::getProfessor).distinct()
                .collect(toList());

        assertThat(actual).usingElementComparatorIgnoringFields("courses")
                .hasSameElementsAs(expected);
    }

    @Test
    public void scheduleTemplatesShouldHaveNoAuditoriumCollisions() {

        List<ScheduleTemplate> templates = templateRepository.findAll();

        List<DayOfWeek> workDays = Arrays.asList(DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
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

        List<ScheduleTemplate> templates = templateRepository.findAll();

        List<DayOfWeek> workDays = Arrays.asList(DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
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

        List<ScheduleTemplate> templates = templateRepository.findAll();

        List<DayOfWeek> workDays = Arrays.asList(DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
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
