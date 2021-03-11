package com.shablii.timetable.service.model.generator;

import com.shablii.timetable.model.*;
import com.shablii.timetable.service.TimetableFacade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import javax.transaction.Transactional;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.atLeastOnce;

@SpringBootTest
class UniversityModelGeneratorTest {

    @SpyBean
    private TimetableFacade timetableFacade;

    @Autowired
    private UniversityModelGenerator universityModelGenerator;

    @Test
    @Transactional
    public void generateAndSaveShouldSaveGeneratedDataToRepository() {

        timetableFacade.deleteAllData();

        universityModelGenerator.generateAndSave();

        then(timetableFacade).should(atLeastOnce()).saveAuditoriums(anyList());
        then(timetableFacade).should(atLeastOnce()).saveCourses(anyList());
        then(timetableFacade).should(atLeastOnce()).saveProfessors(anyList());
        then(timetableFacade).should(atLeastOnce()).saveGroups(anyList());
        then(timetableFacade).should(atLeastOnce()).saveStudents(anyList());
    }

    @Test
    public void modelShouldContainCorrectAmountOfItems() {

        int expectedNumberOfAuditoriums = 5;
        int expectedNumberOfCourses = 10;
        int expectedNumberOfGroups = 10;
        int expectedNumberOfProfessors = 5;
        int expectedNumberOfStudents = 300;

        long actualNumberOfAuditoriums = timetableFacade.countAuditoriums();
        long actualNumberOfCourses = timetableFacade.countCourses();
        long actualNumberOfGroups = timetableFacade.countGroups();
        long actualNumberOfProfessors = timetableFacade.countProfessors();
        long actualNumberOfStudents = timetableFacade.countStudents();

        assertThat(actualNumberOfAuditoriums).isEqualTo(expectedNumberOfAuditoriums);
        assertThat(actualNumberOfCourses).isEqualTo(expectedNumberOfCourses);
        assertThat(actualNumberOfGroups).isEqualTo(expectedNumberOfGroups);
        assertThat(actualNumberOfProfessors).isEqualTo(expectedNumberOfProfessors);
        assertThat(actualNumberOfStudents).isEqualTo(expectedNumberOfStudents);
    }

    @Test
    public void modelShouldContainCorrectListOfCourses() {

        List<String> expected = Arrays.asList("Scientology", "Procrastination101", "Demagoguery", "Dark Magic",
                "Defense Against Dark Magic", "Dark Magic Against Defense Against Dark Magic",
                "Theoretical Camel Tracking", "Applied Polyandry", "DB Sanitation'); DROP TABLE students; --",
                "Modern Sand Castle Architecture");

        List<Course> actual = timetableFacade.getCourses();

        assertThat(actual).extracting(Course::getName).hasSameElementsAs(expected);
    }

    @Test
    public void modelShouldContainGroupsInCorrectFormat() {

        String groupNamePattern = "^G-\\d{2}$";

        List<Group> actual = timetableFacade.getGroups();

        assertThat(actual).extracting(Group::getName).allMatch(name -> name.matches(groupNamePattern));
    }

    @Test
    public void modelShouldContainAuditoriumsInCorrectFormat() {

        String auditoriumNamePattern = "^A-\\d{2}$";

        List<Auditorium> actual = timetableFacade.getAuditoriums();

        assertThat(actual).extracting(Auditorium::getName).allMatch(name -> name.matches(auditoriumNamePattern));
    }

    @Test
    public void modelShouldContainStudentsCombinedFromNamesFiles() {

        List<String> expectedFirstNames = Arrays.asList("Sumu-abum", "Hammurabi", "Ishbibal", "Shushushi",
                "Peshgaldaramesh", "Ayadaragalama", "Melamkurkurra", "Karaindash", "Marduk-apla-iddina",
                "Nebuchadnezzar", "Nabu-shum-libur", "Baba-aha-iddina", "Nabonassar", "Shalmaneser", "Sennacherib",
                "Esarhaddon", "Ashurbanipal", "Sinsharishkun", "Nabopolassar", "Darius");
        List<String> expectedLastNames = Arrays.asList("the I", "the II", "the III", "the Great", "the Majestic",
                "the Tall", "the Gracious", "the Wise", "the Wisdomous", "the Kind", "the Fast", "the Powerful",
                "the Sneaky", "the Preposterous", "Strangelove", "Sandlicker", "Nosepicker", "Footsticker",
                "Buttspanker", "Eyetwitcher");

        List<Student> actual = timetableFacade.getStudents();

        assertThat(actual).extracting(Student::getFirstName).isSubsetOf(expectedFirstNames);
        assertThat(actual).extracting(Student::getLastName).isSubsetOf(expectedLastNames);
    }

    @Test
    public void modelShouldContainProfessorsCombinedFromNamesFiles() {

        List<String> expectedFirstNames = Arrays.asList("Sumu-abum", "Hammurabi", "Ishbibal", "Shushushi",
                "Peshgaldaramesh", "Ayadaragalama", "Melamkurkurra", "Karaindash", "Marduk-apla-iddina",
                "Nebuchadnezzar", "Nabu-shum-libur", "Baba-aha-iddina", "Nabonassar", "Shalmaneser", "Sennacherib",
                "Esarhaddon", "Ashurbanipal", "Sinsharishkun", "Nabopolassar", "Darius");
        List<String> expectedLastNames = Arrays.asList("the I", "the II", "the III", "the Great", "the Majestic",
                "the Tall", "the Gracious", "the Wise", "the Wisdomous", "the Kind", "the Fast", "the Powerful",
                "the Sneaky", "the Preposterous", "Strangelove", "Sandlicker", "Nosepicker", "Footsticker",
                "Buttspanker", "Eyetwitcher");

        List<Professor> actual = timetableFacade.getProfessors();

        assertThat(actual).extracting(Professor::getFirstName).isSubsetOf(expectedFirstNames);
        assertThat(actual).extracting(Professor::getLastName).isSubsetOf(expectedLastNames);
    }

    @Test
    public void eachStudentShouldHaveAGroup() {

        List<Student> actual = timetableFacade.getStudents();

        assertThat(actual).extracting(Student::getGroup).isNotNull();
    }

    @Test
    public void eachGroupShouldHaveUpToThirtyStudents() {

        Map<Group, Long> groupsSizes = timetableFacade.getStudents()
                .stream()
                .map(Student::getGroup)
                .collect(groupingBy(Function.identity(), counting()));

        assertThat(groupsSizes.values()).allMatch(groupSize -> groupSize >= 1 && groupSize <= 30);
    }

    @Test
    public void eachProfessorShouldHaveOneToFourCourses() {

        List<Professor> actual = timetableFacade.getProfessors();

        assertThat(actual).extracting(Professor::getCourses)
                .allMatch(professorCourses -> !professorCourses.isEmpty() && professorCourses.size() <= 4);
    }

    @Test
    public void eachCourseShouldHaveOneToTwoProfessors() {

        Map<Course, Long> coursesAssignments = timetableFacade.getProfessors()
                .stream()
                .flatMap(professor -> professor.getCourses().stream())
                .collect(groupingBy(Function.identity(), counting()));

        assertThat(coursesAssignments.values()).allMatch(
                courseAssignment -> courseAssignment >= 1 && courseAssignment <= 2);
    }

    @Test
    public void eachCourseShouldBeAssigned() {

        List<Course> expected = timetableFacade.getCourses();

        List<Course> actual = timetableFacade.getProfessors()
                .stream()
                .flatMap(professor -> professor.getCourses().stream())
                .distinct()
                .collect(toList());

        assertThat(actual).hasSameElementsAs(expected);
    }

}
