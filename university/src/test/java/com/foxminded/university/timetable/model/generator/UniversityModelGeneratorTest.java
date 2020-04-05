package com.foxminded.university.timetable.model.generator;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import com.foxminded.university.timetable.model.Auditorium;
import com.foxminded.university.timetable.model.Course;
import com.foxminded.university.timetable.model.Group;
import com.foxminded.university.timetable.model.Professor;
import com.foxminded.university.timetable.model.SemesterProperties;
import com.foxminded.university.timetable.model.Student;
import com.foxminded.university.timetable.model.University;

@TestInstance(Lifecycle.PER_CLASS)
class UniversityModelGeneratorTest {
    private UniversityModelGenerator modelGenerator;
    private String firstNamesFilePath = "first_names.txt";
    private String lastNamesFilePath = "last_names.txt";
    private String coursesFilePath = "courses.txt";
    private University universityModel;

    @BeforeAll
    private void setUp() {
        this.modelGenerator = new UniversityModelGenerator();
        universityModel = modelGenerator.generateUniversity(firstNamesFilePath,
                lastNamesFilePath, coursesFilePath);
        LocalDate semesterStartDate = LocalDate.of(2020, 9, 7);
        LocalDate semesterEndDate = LocalDate.of(2020, 12, 11);
        SemesterProperties semesterProperties = new SemesterProperties(
                semesterStartDate, semesterEndDate);
        universityModel.setSemesterProperties(semesterProperties);
    }

    @Nested
    public class InputFileValidationTest {
        @Test
        public void shouldThrowIllegalArgumentExceptionGivenWrongFilePath() {
            String wrongFilePath = "nonexistingFile.txt";

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> modelGenerator.generateUniversity(wrongFilePath,
                            wrongFilePath, wrongFilePath));
            assertEquals("Unable to locate nonexistingFile.txt",
                    exception.getMessage());
        }

        @Test
        public void shouldThrowIllegalArgumentExceptionGivenWrongNonTxtFile() {
            String wrongFile = "wrong_file.log";

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> modelGenerator.generateUniversity(wrongFile,
                            wrongFile, wrongFile));
            assertEquals("wrong_file.log is not a *.txt file",
                    exception.getMessage());
        }

        @Test
        public void shouldThrowIllegalArgumentExceptionGivenEmptyFile() {
            String emptyFile = "empty.txt";

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> modelGenerator.generateUniversity(emptyFile,
                            emptyFile, emptyFile));
            assertEquals("empty.txt appears to be empty",
                    exception.getMessage());
        }
    }

    @Test
    public void modelShouldContainCorrectAmountOfItems() {
        int expectedNumberOfAuditoriums = 5;
        int expectedNumberOfCourses = 10;
        int expectedNumberOfGroups = 10;
        int expectedNumberOfProfessors = 5;
        int expectedNumberOfStudents = 300;
        int expectedNumberOfWeeks = 14;

        int actualNumberOfAuditoriums = universityModel.getAuditoriums().size();
        int actualNumberOfCourses = universityModel.getCourses().size();
        int actualNumberOfGroups = universityModel.getGroups().size();
        int actualNumberOfProfessors = universityModel.getProfessors().size();
        int actualNumberOfStudents = universityModel.getStudents().size();
        int actualNumberOfWeeks = universityModel.getSemesterProperties()
                .getLengthInWeeks();

        assertAll(
                () -> assertEquals(expectedNumberOfAuditoriums,
                        actualNumberOfAuditoriums),
                () -> assertEquals(expectedNumberOfCourses,
                        actualNumberOfCourses),
                () -> assertEquals(expectedNumberOfGroups,
                        actualNumberOfGroups),
                () -> assertEquals(expectedNumberOfProfessors,
                        actualNumberOfProfessors),
                () -> assertEquals(expectedNumberOfStudents,
                        actualNumberOfStudents),
                () -> assertEquals(expectedNumberOfWeeks, actualNumberOfWeeks));
    }

    @Test
    public void modelShouldContainCorrectListOfCourses() {
        List<String> expected = Arrays
                .asList("Scientology", "Procrastination101", "Demagoguery",
                        "Dark Magic", "Defense Against Dark Magic",
                        "Dark Magic Against Defense Against Dark Magic",
                        "Theoretical Camel Tracking", "Applied Polyandry",
                        "DB Sanitation'); DROP TABLE students; --",
                        "Modern Sand Castle Architecture")
                .stream().sorted().collect(Collectors.toList());
        List<String> actual = universityModel.getCourses().stream()
                .map(Course::getName).sorted().collect(Collectors.toList());

        assertEquals(expected, actual);
    }

    @Test
    public void modelShouldContainGroupsInCorrectFormat() {
        String groupNamePattern = "^G-\\d{2}$";

        universityModel.getGroups().stream().map(Group::getName).forEach(
                groupName -> assertTrue(groupName.matches(groupNamePattern)));
    }

    @Test
    public void modelShouldContainAuditoriumsInCorrectFormat() {
        String auditoriumNamePattern = "^A-\\d{2}$";

        universityModel.getAuditoriums().stream().map(Auditorium::getName)
                .forEach(auditoriumName -> assertTrue(
                        auditoriumName.matches(auditoriumNamePattern)));
    }

    @Test
    public void modelShouldContainStudentsCombinedFromNamesFiles() {
        List<String> expectedFirstNames = Arrays.asList("Sumu-abum",
                "Hammurabi", "Ishbibal", "Shushushi", "Peshgaldaramesh",
                "Ayadaragalama", "Melamkurkurra", "Karaindash",
                "Marduk-apla-iddina", "Nebuchadnezzar", "Nabu-shum-libur",
                "Baba-aha-iddina", "Nabonassar", "Shalmaneser", "Sennacherib",
                "Esarhaddon", "Ashurbanipal", "Sinsharishkun", "Nabopolassar",
                "Darius");
        expectedFirstNames.sort(Comparator.naturalOrder());
        List<String> expectedLastNames = Arrays.asList("the I", "the II",
                "the III", "the Great", "the Majestic", "the Tall",
                "the Gracious", "the Wise", "the Wisdomous", "the Kind",
                "the Fast", "the Powerful", "the Sneaky", "the Preposterous",
                "Strangelove", "Sandlicker", "Nosepicker", "Footsticker",
                "Buttspanker", "Eyetwitcher");
        expectedLastNames.sort(Comparator.naturalOrder());
        List<String> actualFirstNames = universityModel.getStudents().stream()
                .map(Student::getFirstName).distinct().sorted()
                .collect(Collectors.toList());
        List<String> actualLastNames = universityModel.getStudents().stream()
                .map(Student::getLastName).distinct().sorted()
                .collect(Collectors.toList());

        assertAll(() -> assertEquals(expectedFirstNames, actualFirstNames),
                () -> assertEquals(expectedLastNames, actualLastNames));
    }

    @Test
    public void modelShouldContainProfessorsCombinedFromNamesFiles() {
        List<String> expectedFirstNames = Arrays.asList("Sumu-abum",
                "Hammurabi", "Ishbibal", "Shushushi", "Peshgaldaramesh",
                "Ayadaragalama", "Melamkurkurra", "Karaindash",
                "Marduk-apla-iddina", "Nebuchadnezzar", "Nabu-shum-libur",
                "Baba-aha-iddina", "Nabonassar", "Shalmaneser", "Sennacherib",
                "Esarhaddon", "Ashurbanipal", "Sinsharishkun", "Nabopolassar",
                "Darius");
        List<String> expectedLastNames = Arrays.asList("the I", "the II",
                "the III", "the Great", "the Majestic", "the Tall",
                "the Gracious", "the Wise", "the Wisdomous", "the Kind",
                "the Fast", "the Powerful", "the Sneaky", "the Preposterous",
                "Strangelove", "Sandlicker", "Nosepicker", "Footsticker",
                "Buttspanker", "Eyetwitcher");
        List<String> actualFirstNames = universityModel.getProfessors().stream()
                .map(Professor::getFirstName).distinct()
                .collect(Collectors.toList());
        List<String> actualLastNames = universityModel.getProfessors().stream()
                .map(Professor::getLastName).distinct()
                .collect(Collectors.toList());

        assertAll(
                () -> assertTrue(actualFirstNames.stream().allMatch(
                        firstName -> expectedFirstNames.contains(firstName))),
                () -> assertTrue(actualLastNames.stream().allMatch(
                        lastName -> expectedLastNames.contains(lastName))));
    }

    @Test
    public void eachStudentShouldHaveAGroup() {
        boolean eachStudentHasAGroup = universityModel.getStudents().stream()
                .map(Student::getGroup)
                .allMatch(group -> group instanceof Group && group != null);

        assertTrue(eachStudentHasAGroup);
    }

    @Test
    public void eachGroupShouldHaveUpToThirtyStudents() {
        boolean eachGroupHasUpToThirtyStudents = universityModel.getStudents()
                .stream().map(Student::getGroup)
                .collect(Collectors.groupingBy(Function.identity(),
                        Collectors.counting()))
                .values().stream()
                .allMatch(groupOccurance -> groupOccurance >= 1
                        && groupOccurance <= 30);

        assertTrue(eachGroupHasUpToThirtyStudents);
    }

    @Test
    public void eachGroupShouldBeEnrolledInAllCourses() {
        boolean eachGroupAssignedToAllCourses = universityModel.getGroups()
                .stream().map(Group::getCourses).allMatch(courses -> courses
                        .containsAll(universityModel.getCourses()));
        boolean groupAssignedToWrongCourse = universityModel.getGroups()
                .stream().flatMap(group -> group.getCourses().stream())
                .filter(course -> !universityModel.getCourses()
                        .contains(course))
                .findAny().isPresent();

        assertTrue(eachGroupAssignedToAllCourses);
        assertFalse(groupAssignedToWrongCourse);
    }

    @Test
    public void eachProfessorShouldHaveOneToThreeCourses() {
        boolean eachProfessorHasOneToThreeCourses = universityModel
                .getProfessors().stream().map(Professor::getCourses).allMatch(
                        courses -> courses.size() >= 1 && courses.size() <= 4);

        assertTrue(eachProfessorHasOneToThreeCourses);
    }

    @Test
    public void eachCourseShouldHaveOneToTwoProfessors() {
        boolean eachCourseHasOneToTwoProfessors = universityModel
                .getProfessors().stream()
                .flatMap(professor -> professor.getCourses().stream())
                .collect(Collectors.groupingBy(Function.identity(),
                        Collectors.counting()))
                .values().stream()
                .allMatch(courseOccurance -> courseOccurance >= 1
                        && courseOccurance <= 2);

        assertTrue(eachCourseHasOneToTwoProfessors);
    }

    @Test
    public void eachCourseShouldBeAssigned() {
        List<Course> expected = (List<Course>) universityModel.getCourses();
        expected.sort(Course::compareTo);
        List<Course> actual = universityModel.getProfessors().stream()
                .flatMap(professor -> professor.getCourses().stream())
                .distinct().sorted().collect(Collectors.toList());

        assertEquals(expected, actual);
    }
}
