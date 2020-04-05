package com.foxminded.university.timetable.model.generator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.foxminded.university.timetable.model.Auditorium;
import com.foxminded.university.timetable.model.Course;
import com.foxminded.university.timetable.model.Group;
import com.foxminded.university.timetable.model.Professor;
import com.foxminded.university.timetable.model.Student;
import com.foxminded.university.timetable.model.University;

public class UniversityModelGenerator {
    private static final int NUMBER_OF_STUDENTS = 300;
    private static final int NUMBER_OF_PROFESSORS = 5;
    private static final int NUMBER_OF_AUDITORIUMS = 5;
    private static final int GROUP_SIZE = 30;

    public University generateUniversity(String firstNamesFilePath,
            String lastNamesFilePath, String coursesFilePath) {
        List<Auditorium> auditoriums = buildAuditoriums();
        List<Course> courses = buildCourses(coursesFilePath);
        List<Professor> professors = buildProfessors(firstNamesFilePath,
                lastNamesFilePath);
        assignCoursesToProfessors(courses, professors);
        List<Group> groups = buildGroups(courses);
        List<Student> students = buildStudents(firstNamesFilePath,
                lastNamesFilePath);
        groupStudents(students, groups);

        return new University(auditoriums, courses, groups, professors,
                students);
    }

    /*
     * Randomly assign 1-3 courses per professor and 1-2 professors per course
     */
    private void assignCoursesToProfessors(List<Course> courses,
            List<Professor> professors) {
        Random random = new Random();
        int maxCourses = 4;
        int maxProfessors = 2;

        for (Course course : courses) {
            int professorsToAssign = random.nextInt(maxProfessors);
            for (int i = 0; i <= professorsToAssign; i++) {
                Optional<Professor> professor = professors.stream()
                        .filter(p -> p.getCourses().size() < maxCourses
                                && !p.getCourses().contains(course))
                        .sorted(Comparator
                                .comparing(prof -> prof.getCourses().size()))
                        .findFirst();
                if (professor.isPresent()) {
                    professor.get().addCourse(course);
                }
            }
        }
    }

    /*
     * 200 students. Take 20 first names and 20 last names and randomly combine
     * them to generate students
     */
    private List<Student> buildStudents(String firstNamesFilePath,
            String lastNamesFilePath) {
        List<String> firstNames = readFile(firstNamesFilePath);
        List<String> lastNames = readFile(lastNamesFilePath);
        Random random = new Random();
        List<Student> students = new ArrayList<>(NUMBER_OF_STUDENTS);

        for (int i = 0; i < NUMBER_OF_STUDENTS; i++) {
            String firstName = firstNames
                    .get(random.nextInt(firstNames.size()));
            String lastName = lastNames.get(random.nextInt(lastNames.size()));

            students.add(new Student(firstName, lastName));
        }
        return students;
    }

    /*
     * 5 professors. Take 20 first names and 20 last names and randomly combine
     * them to generate professors
     */
    private List<Professor> buildProfessors(String firstNamesFilePath,
            String lastNamesFilePath) {
        List<String> firstNames = readFile(firstNamesFilePath);
        List<String> lastNames = readFile(lastNamesFilePath);
        Random random = new Random();
        List<Professor> professors = new ArrayList<>(NUMBER_OF_PROFESSORS);

        for (int i = 0; i < NUMBER_OF_PROFESSORS; i++) {
            String firstName = firstNames
                    .get(random.nextInt(firstNames.size()));
            String lastName = lastNames.get(random.nextInt(lastNames.size()));

            professors.add(new Professor(firstName, lastName));
        }
        return professors;
    }

    /*
     * Group students by last name
     */
    private void groupStudents(List<Student> students, List<Group> groups) {
        groups.forEach(group -> students.stream()
                .sorted(Comparator.comparing(Student::getLastName))
                .skip(groups.indexOf(group) * (long) GROUP_SIZE)
                .limit(GROUP_SIZE).forEach(student -> student.setGroup(group)));
    }

    private List<Course> buildCourses(String filePath) {
        return readFile(filePath).stream().map(Course::new)
                .collect(Collectors.toList());
    }

    /*
     * Groups of max 30 students, naming convention: 'G-01', 'G-02',... Every
     * group takes all courses
     */
    private List<Group> buildGroups(List<Course> courses) {
        int numberOfGroups = (int) Math
                .ceil((double) NUMBER_OF_STUDENTS / (double) GROUP_SIZE);
        List<Group> groups = new ArrayList<>(numberOfGroups);

        for (int groupId = 1; groupId <= numberOfGroups; groupId++) {
            char groupInitial = 'G';
            String groupName = String.format("%c-%02d", groupInitial, groupId);
            groups.add(new Group(groupName, courses));
        }
        return groups;
    }
    
    /*
     * Auditorium naming convention: 'A-01', 'A-02', etc.
     */
    private List<Auditorium> buildAuditoriums(){
        List<Auditorium> auditoriums = new ArrayList<>();
        for (int id = 1; id <= NUMBER_OF_AUDITORIUMS; id++) {
            String name = String.format("A-%02d", id);
            auditoriums.add(new Auditorium(name));
        }
        
        return auditoriums;
    }

    private List<String> readFile(String filePath) {
        URL url = validateUrl(filePath);

        try (Stream<String> fileStream = Files.lines(Paths.get(url.toURI()))) {
            List<String> fileLines = fileStream.collect(Collectors.toList());

            if (fileLines.isEmpty()) {
                throw new IllegalArgumentException(
                        filePath + " appears to be empty");
            }

            return fileLines;
        } catch (IOException | URISyntaxException e) {
            throw new IllegalArgumentException("Unable to read " + filePath, e);
        }
    }

    private URL validateUrl(String filePath) {
        if (!filePath.matches(".+\\.txt$")) {
            throw new IllegalArgumentException(
                    filePath + " is not a *.txt file");
        }
        URL url = getClass().getClassLoader().getResource(filePath);
        if (url == null) {
            throw new IllegalArgumentException("Unable to locate " + filePath);
        }

        return url;
    }
}
