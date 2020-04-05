package com.foxminded.university.timetable.model.generator;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import com.foxminded.university.timetable.model.Auditorium;
import com.foxminded.university.timetable.model.Course;
import com.foxminded.university.timetable.model.Group;
import com.foxminded.university.timetable.model.Professor;
import com.foxminded.university.timetable.model.ReschedulingOption;
import com.foxminded.university.timetable.model.ScheduleTemplate;
import com.foxminded.university.timetable.model.SemesterProperties;
import com.foxminded.university.timetable.model.Timetable;
import com.foxminded.university.timetable.model.University;

@TestInstance(Lifecycle.PER_CLASS)
class TimetableModelGeneratorTest {
    private UniversityModelGenerator modelGenerator;
    private String firstNamesFilePath = "first_names.txt";
    private String lastNamesFilePath = "last_names.txt";
    private String coursesFilePath = "courses.txt";
    private University university;
    private TimetableModelGenerator timetableModelGenerator;
    private Timetable timetable;
    private boolean[] weekParity = new boolean[] { true, false };

    @BeforeAll
    private void setUp() {
        this.modelGenerator = new UniversityModelGenerator();
        this.university = modelGenerator.generateUniversity(firstNamesFilePath,
                lastNamesFilePath, coursesFilePath);
        LocalDate semesterStartDate = LocalDate.of(2020, 9, 7);
        LocalDate semesterEndDate = LocalDate.of(2020, 12, 11);
        SemesterProperties semesterProperties = new SemesterProperties(
                semesterStartDate, semesterEndDate);
        university.setSemesterProperties(semesterProperties);
        this.timetableModelGenerator = new TimetableModelGenerator(university);
        this.timetable = timetableModelGenerator.generateTimetable();
    }

    @Test
    public void timetableShouldHaveHundredTwentyFiveReschedulingOptions() {
        int expected = 125;
        int actual = timetable.getReschedulingOptions().size();

        assertEquals(expected, actual);
    }

    @Test
    public void timetableShouldHaveFiveReschedulingOptionsPerPeriod() {
        boolean fiveOptionsPerDayPerPeriod = timetable.getReschedulingOptions()
                .stream()
                .collect(groupingBy(ReschedulingOption::getDay,
                        groupingBy(ReschedulingOption::getPeriod, counting())))
                .values().stream()
                .flatMap(periodOptions -> periodOptions.values().stream())
                .allMatch(optionsPerPeriod -> optionsPerPeriod == 5L);

        assertTrue(fiveOptionsPerDayPerPeriod);
    }

    @Test
    public void timetableShouldHaveTwentyFiveReschedulingOptionsPerDay() {
        boolean twentyFiveOptionsPerDay = timetable.getReschedulingOptions()
                .stream()
                .collect(groupingBy(ReschedulingOption::getDay, counting()))
                .values().stream()
                .allMatch(optionsPerDay -> optionsPerDay == 25);

        assertTrue(twentyFiveOptionsPerDay);
    }

    @Test
    public void timetableReschedulingOptionsShouldHaveWorkDaysOnly() {
        boolean noReschedulingOptionsForWeekends = timetable
                .getReschedulingOptions().stream()
                .map(ReschedulingOption::getDay)
                .noneMatch(day -> day == DayOfWeek.SATURDAY
                        || day == DayOfWeek.SUNDAY);

        assertTrue(noReschedulingOptionsForWeekends);
    }

    @Test
    public void timetableScheduleTemplatesShouldContainAllGroups() {
        List<Group> expected = university.getGroups().stream().sorted()
                .collect(toList());
        List<Group> actual = timetable.getScheduleTemplates().stream()
                .map(ScheduleTemplate::getGroup).distinct().sorted()
                .collect(toList());

        assertEquals(expected, actual);
    }

    @Test
    public void timetableScheduleTemplatesShouldContainAllCourses() {
        List<Course> expected = university.getCourses().stream().sorted()
                .collect(Collectors.toList());
        List<Course> actual = timetable.getScheduleTemplates().stream()
                .map(ScheduleTemplate::getCourse).distinct().sorted()
                .collect(toList());

        assertEquals(expected, actual);
    }

    @Test
    public void timetableScheduleTemplatesShouldContainAllCoursesForEachGroup() {
        List<Course> expected = university.getCourses().stream().sorted()
                .collect(toList());
        Map<Group, List<Course>> eachGroupCourses = timetable
                .getScheduleTemplates().stream()
                .sorted(Comparator.comparing(ScheduleTemplate::getCourse))
                .collect(groupingBy(ScheduleTemplate::getGroup,
                        mapping(ScheduleTemplate::getCourse, toList())));

        for (List<Course> groupCourses : eachGroupCourses.values()) {
            assertEquals(expected, groupCourses);
        }
    }

    @Test
    public void timetableScheduleTemplatesShouldContainAllProfessors() {
        List<Professor> expected = university.getProfessors().stream().sorted()
                .collect(Collectors.toList());
        List<Professor> actual = timetable.getScheduleTemplates().stream()
                .map(ScheduleTemplate::getProfessor).distinct().sorted()
                .collect(toList());

        assertEquals(expected, actual);
    }

    @Test
    public void timetableScheduleTemplatesShouldHaveNoAuditoriumCollisions() {
        for (boolean parity : weekParity) {
            for (Auditorium auditorium : university.getAuditoriums()) {
                List<ScheduleTemplate> auditoriumSchedules = timetable
                        .getScheduleTemplates().stream()
                        .filter(template -> template.getWeekParity() == parity
                                && template.getAuditorium().equals(auditorium))
                        .collect(toList());
                long expected = auditoriumSchedules.size();
                long uniqueTimePlaceTemplates = auditoriumSchedules
                        .stream()
                        .map(template -> new ReschedulingOption(
                                template.getDay(), template.getPeriod(),
                                template.getAuditorium()))
                        .distinct().count();

                assertEquals(expected, uniqueTimePlaceTemplates);
            }
        }
    }

    @Test
    public void timetableScheduleTemplatesShouldHaveNoProfessorCollisions() {
        for (boolean parity : weekParity) {
            for (Professor professor : university.getProfessors()) {
                List<ScheduleTemplate> professorSchedules = timetable
                        .getScheduleTemplates().stream()
                        .filter(template -> template.getWeekParity() == parity
                                && template.getProfessor().equals(professor))
                        .collect(toList());
                long expected = professorSchedules.size();
                long uniqueProfessorTimePlaceTemplates = professorSchedules
                        .stream()
                        .map(template -> new ReschedulingOption(
                                template.getDay(), template.getPeriod(),
                                template.getAuditorium()))
                        .distinct().count();

                assertEquals(expected, uniqueProfessorTimePlaceTemplates);
            }
        }
    }

    @Test
    public void timetableScheduleTemplatesShouldHaveNoGroupCollisions() {
        for (boolean parity : weekParity) {
            for (Group group : university.getGroups()) {
                List<ScheduleTemplate> groupTemplates = timetable
                        .getScheduleTemplates().stream()
                        .filter(template -> template.getWeekParity() == parity
                                && template.getGroup().equals(group))
                        .collect(toList());
                long expected = groupTemplates.size();
                long uniqueGroupTimePlaceTemplates = groupTemplates.stream()
                        .map(template -> new ReschedulingOption(
                                template.getDay(), template.getPeriod(),
                                template.getAuditorium()))
                        .distinct().count();

                assertEquals(expected, uniqueGroupTimePlaceTemplates);
            }
        }
    }
}
