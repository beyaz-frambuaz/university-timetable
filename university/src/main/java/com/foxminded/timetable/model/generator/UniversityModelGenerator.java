package com.foxminded.timetable.model.generator;

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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.foxminded.timetable.dao.jdbc.Repositories;
import com.foxminded.timetable.model.Auditorium;
import com.foxminded.timetable.model.Course;
import com.foxminded.timetable.model.Group;
import com.foxminded.timetable.model.Professor;
import com.foxminded.timetable.model.Student;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Setter
@Service
@RequiredArgsConstructor
public class UniversityModelGenerator {

    @Value("${university.total.students}")
    private int totalStudents;
    @Value("${university.total.professors}")
    private int totalProfessors;
    @Value("${university.total.auditoriums}")
    private int totalAuditoriums;
    @Value("${university.group.size}")
    private int groupSize;
    @Value("${file.first.names}")
    private String firstNamesFilePath;
    @Value("${file.last.names}")
    private String lastNamesFilePath;
    @Value("${file.courses}")
    private String coursesFilePath;

    private final Repositories repositories;

    public void generateAndSave() {
        
        log.info("Generating university model");
        buildAuditoriums();
        List<Course> courses = buildCourses(coursesFilePath);
        List<Professor> professors = buildProfessors(firstNamesFilePath,
                lastNamesFilePath);
        assignCoursesToProfessors(courses, professors);
        List<Group> groups = buildGroups();
        List<Student> students = buildStudents(firstNamesFilePath,
                lastNamesFilePath);
        groupAndSaveStudents(students, groups);
        log.info("University model generated");
        
    }

    /*
     * Randomly assign 1-4 courses per professor and 1-2 professors per course
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
        log.debug("Courses assigned to professors");
        repositories.getProfessorRepository()
                .saveAllProfessorsCourses(professors);
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
        List<Student> students = new ArrayList<>(totalStudents);

        for (int i = 0; i < totalStudents; i++) {
            String firstName = firstNames
                    .get(random.nextInt(firstNames.size()));
            String lastName = lastNames.get(random.nextInt(lastNames.size()));

            students.add(new Student(firstName, lastName));
        }
        log.debug("Students generated");
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
        List<Professor> professors = new ArrayList<>(totalProfessors);

        for (int i = 0; i < totalProfessors; i++) {
            String firstName = firstNames
                    .get(random.nextInt(firstNames.size()));
            String lastName = lastNames.get(random.nextInt(lastNames.size()));

            professors.add(new Professor(firstName, lastName));
        }
        log.debug("Professors generated");
        repositories.getProfessorRepository().saveAll(professors);
        
        return repositories.getProfessorRepository().findAll();
    }

    /*
     * Group students by last name
     */
    private void groupAndSaveStudents(List<Student> students,
            List<Group> groups) {
        
        groups.forEach(group -> students.stream()
                .sorted(Comparator.comparing(Student::getLastName))
                .skip(groups.indexOf(group) * (long) groupSize).limit(groupSize)
                .forEach(student -> student.setGroup(group)));
        log.debug("Students grouped");
        
        repositories.getStudentRepository().saveAll(students);
    }

    private List<Course> buildCourses(String filePath) {
        
        List<Course> courses = readFile(filePath).stream().map(Course::new)
                .collect(Collectors.toList());
        log.debug("Courses generated");
        repositories.getCourseRepository().saveAll(courses);
        return repositories.getCourseRepository().findAll();
    }

    /*
     * Groups of max 30 students, naming convention: 'G-01', 'G-02',... Every
     * group takes all courses
     */
    private List<Group> buildGroups() {
        
        int numberOfGroups = (int) Math
                .ceil((double) totalStudents / (double) groupSize);
        List<Group> groups = new ArrayList<>(numberOfGroups);

        for (int groupId = 1; groupId <= numberOfGroups; groupId++) {
            char groupInitial = 'G';
            String groupName = String.format("%c-%02d", groupInitial, groupId);
            groups.add(new Group(groupName));
        }
        log.debug("Groups generated");
        repositories.getGroupRepository().saveAll(groups);
        
        return repositories.getGroupRepository().findAll();
    }

    /*
     * Auditorium naming convention: 'A-01', 'A-02', etc.
     */
    private void buildAuditoriums() {
        
        List<Auditorium> auditoriums = new ArrayList<>();
        for (int id = 1; id <= totalAuditoriums; id++) {
            String name = String.format("A-%02d", id);
            auditoriums.add(new Auditorium(name));
        }
        log.debug("Auditoriums generated");
        repositories.getAuditoriumRepository().saveAll(auditoriums);
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
            log.error("Unable to read {}", filePath);
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
            log.error("Unable to locate {}", filePath);
            throw new IllegalArgumentException("Unable to locate " + filePath);
        }

        return url;
    }
}
