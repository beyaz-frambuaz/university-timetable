package com.foxminded.university.timetable.menu;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.foxminded.university.timetable.model.Auditorium;
import com.foxminded.university.timetable.model.Course;
import com.foxminded.university.timetable.model.Period;
import com.foxminded.university.timetable.model.Professor;
import com.foxminded.university.timetable.model.ReschedulingOption;
import com.foxminded.university.timetable.model.Schedule;
import com.foxminded.university.timetable.model.ScheduleTemplate;
import com.foxminded.university.timetable.model.Student;
import com.foxminded.university.timetable.model.University;
import com.foxminded.university.timetable.printer.Printer;

public class MenuManager {
    private static final String MAIN_MENU = "\n1. Display two-week schedule\n"
            + "2. Display schedule\n" + "3. Display course attendees\n"
            + "4. Display available auditoriums\n"
            + "5. Display available professors\n" + "6. Reschedule course\n"
            + "7. Substitute professor\n" + "0. Exit\n";
    private static final Scanner SCANNER = new Scanner(System.in)
            .useDelimiter("\\n");
    private static final String SCHEDULE_MODE = "\nPick schedule mode:\n"
            + "1. Day\n2. Week\n3. Month\n";
    private static final String SCHEDULE_TARGET = "\nSchedule for:\n"
            + "1. Student\n2. Professor\n3. Auditorium\n4. No filter\n";
    private final University university;
    private final Printer printer;
    private final InputCollector inputCollector;

    public MenuManager(University university, Printer printer) {
        this(university, printer, new InputCollector(SCANNER));
    }

    private MenuManager(University university, Printer printer,
            InputCollector inputCollector) {
        this.printer = printer;
        this.university = university;
        this.inputCollector = inputCollector;
    }

    public boolean loadMainMenu() {
        System.out.println(MAIN_MENU);
        int choice = inputCollector.requestOption(0, 7);

        switch (choice) {
        case 0:
            SCANNER.close();
            System.out.println(
                    "Don't we all have more exciting things to do :)\nCheers!");
            return false;
        case 1:
            displayTwoWeekSchedule();
            break;
        case 2:
            displaySchedule();
            break;
        case 3:
            displayCourseAttendees();
            break;
        case 4:
            displayAvailableAuditoriums();
            break;
        case 5:
            displayAvailableProfessors();
            break;
        case 6:
            rescheduleMenu();
            break;
        case 7:
            substituteProfessor();
            break;
        default:
            break;
        }
        return true;
    }

    private List<Schedule> displaySchedule() {
        System.out.println(SCHEDULE_MODE);
        int scheduleMode = inputCollector.requestOption(1, 3);
        switch (scheduleMode) {
        case 1:
            LocalDate date = inputCollector.requestDate(
                    university.getSemesterProperties().getStartDate(),
                    university.getSemesterProperties().getEndDate());
            return displayScheduleForTarget(date, date);
        case 2:
            System.out.println("Enter week number: ");
            int week = inputCollector.requestOption(1,
                    university.getSemesterProperties().getLengthInWeeks());
            LocalDate monday = university.getTimetable().getWeekMonday(week);
            LocalDate friday = university.getTimetable().getWeekFriday(week);
            return displayScheduleForTarget(monday, friday);
        case 3:
            System.out.println("Enter month number: ");
            int month = inputCollector.requestOption(
                    university.getSemesterProperties().getStartDate()
                            .getMonthValue(),
                    university.getSemesterProperties().getEndDate()
                            .getMonthValue());
            LocalDate firstOfMonth = university.getTimetable()
                    .getMonthStartDate(month);
            LocalDate lastOfMonth = university.getTimetable()
                    .getMonthEndDate(month);
            return displayScheduleForTarget(firstOfMonth, lastOfMonth);
        default:
            return Collections.emptyList();
        }
    }

    private List<Schedule> displayTwoWeekSchedule() {
        List<ScheduleTemplate> twoWeekSchedule = university.getTimetable()
                .getScheduleTemplates();
        twoWeekSchedule.sort(Comparator.naturalOrder());
        System.out.println(printer.printTwoWeekSchedule(twoWeekSchedule));
        return Collections.emptyList();
    }

    private List<Schedule> displayScheduleForTarget(LocalDate start,
            LocalDate end) {
        System.out.println(SCHEDULE_TARGET);
        int scheduleTarget = inputCollector.requestOption(1, 4);
        switch (scheduleTarget) {
        case 1:
            System.out.println("Pick a student ID: ");
            int studentId = inputCollector.requestOption(1,
                    university.getStudents().size());
            Student student = university.getStudents().get(studentId - 1);
            List<Schedule> studentSchedule = university
                    .getStudentSchedule(student, start, end);
            System.out.println(printer.printSchedules(studentSchedule));
            return studentSchedule;
        case 2:
            System.out.println(
                    printer.printProfessors(university.getProfessors()));
            System.out.println("Pick a professor ID: ");
            int professorId = inputCollector.requestOption(1,
                    university.getProfessors().size());
            Professor professor = university.getProfessors()
                    .get(professorId - 1);
            List<Schedule> professorSchedule = university
                    .getProfessorSchedule(professor, start, end);
            System.out.println(printer.printSchedules(professorSchedule));
            return professorSchedule;
        case 3:
            List<Auditorium> auditoriums = university.getAuditoriums();
            System.out.println(printer
                    .printAuditoriums(auditoriums));
            System.out.println("Pick an auditorium ID: ");
            int auditoriumId = inputCollector.requestOption(1,
                    auditoriums.size());
            Auditorium auditorium = auditoriums.get(auditoriumId - 1);
            List<Schedule> auditoriumSchedule = university
                    .getAuditoriumSchedule(auditorium, start, end);
            System.out.println(printer.printSchedules(auditoriumSchedule));
            return auditoriumSchedule;
        case 4:
            List<Schedule> schedules = university.getTimetable()
                    .getRangeSchedule(start, end);
            System.out.println(printer.printSchedules(schedules));
            return schedules;
        default:
            return Collections.emptyList();
        }
    }

    private void displayCourseAttendees() {
        System.out.println(printer.printProfessors(university.getProfessors()));
        System.out.println("Pick a professor ID: ");
        int professorId = inputCollector.requestOption(1,
                university.getProfessors().size());
        Professor professor = university.getProfessors().get(professorId - 1);

        List<Course> courses = professor.getCourses();
        System.out.println(printer.printCourses(courses));
        System.out.println("Pick a course:");
        int courseId = inputCollector.requestOption(1, courses.size());
        Course course = courses.get(courseId - 1);
        List<Student> attendees = university.getCourseAttendees(course,
                professor);

        System.out.println(printer.printStudents(attendees));
    }

    private void displayAvailableAuditoriums() {
        LocalDate date = inputCollector.requestDate(
                university.getSemesterProperties().getStartDate(),
                university.getSemesterProperties().getEndDate());
        System.out.println(printer.printPeriods());
        int periodId = inputCollector.requestOption(1, Period.values().length);
        Period period = Period.values()[periodId - 1];
        List<Auditorium> availableAuditoriums = university
                .getAvailableAuditoriums(date, period);
        System.out.println(printer.printAuditoriums(availableAuditoriums));
    }

    private void displayAvailableProfessors() {
        LocalDate date = inputCollector.requestDate(
                university.getSemesterProperties().getStartDate(),
                university.getSemesterProperties().getEndDate());
        System.out.println(printer.printPeriods());
        int periodId = inputCollector.requestOption(1, Period.values().length);
        Period period = Period.values()[periodId - 1];
        List<Professor> availableProfessors = university
                .getAvailableProfessors(date, period);
        System.out.println(printer.printProfessors(availableProfessors));
    }

    private void rescheduleMenu() {
        List<Schedule> schedules = displaySchedule();
        int scheduleId = inputCollector.requestOption(1, schedules.size());
        Schedule candidate = schedules.get(scheduleId - 1);

        System.out.println("\nSee rescheduling options for:\n1. Date\n"
                + "2. Week\n3. Cancel\n");
        int choice = inputCollector.requestOption(1, 3);
        switch (choice) {
        case 1:
            LocalDate date = inputCollector.requestDate(
                    university.getSemesterProperties().getStartDate(),
                    university.getSemesterProperties().getEndDate());

            rescheduleCourse(candidate, date, date);
            break;
        case 2:
            System.out.println("Enter week number: ");
            int week = inputCollector.requestOption(1,
                    university.getSemesterProperties().getLengthInWeeks());
            LocalDate monday = university.getTimetable().getWeekMonday(week);
            LocalDate friday = university.getTimetable().getWeekFriday(week);

            rescheduleCourse(candidate, monday, friday);
            break;
        case 3:
            break;
        default:
            break;
        }
    }

    private void rescheduleCourse(Schedule candidate, LocalDate startDate,
            LocalDate endDate) {
        Map<LocalDate, List<ReschedulingOption>> weekOptions = university
                .getTimetable()
                .getReschedulingOptions(candidate, startDate, endDate);
        System.out.println(printer.printReschedulingOptions(weekOptions));
        LocalDate targetDate = inputCollector.requestDate(startDate, endDate);
        int optionId = inputCollector.requestOption(1,
                weekOptions.get(targetDate).size());
        ReschedulingOption option = weekOptions.get(targetDate)
                .get(optionId - 1);
        rescheduleByOption(candidate, targetDate, option);
    }

    private void rescheduleByOption(Schedule candidate, LocalDate targetDate,
            ReschedulingOption targetOption) {
        System.out.println("Reschedule:\n1. Once\n2. Permanently\n3. Cancel\n");
        int choice = inputCollector.requestOption(1, 3);
        switch (choice) {
        case 1:
            university.getTimetable().rescheduleOnce(candidate, targetDate,
                    targetOption);
            System.out.println("Done!");
            System.out
                    .println(printer.printSchedules(Arrays.asList(candidate)));
            break;
        case 2:
            university.getTimetable().reschedulePermanently(candidate,
                    targetDate, targetOption);
            System.out.println("Done!");
            System.out
                    .println(printer.printSchedules(Arrays.asList(candidate)));
            break;
        case 3:
            break;
        default:
            break;
        }
    }

    private void substituteProfessor() {
        List<Schedule> schedules = displaySchedule();
        int scheduleId = inputCollector.requestOption(1, schedules.size());
        Schedule candidate = schedules.get(scheduleId - 1);

        List<Professor> availableProfessors = university.getAvailableProfessors(
                candidate.getDate(), candidate.getPeriod());
        System.out.println(printer.printProfessors(availableProfessors));
        int professorId = inputCollector.requestOption(1,
                availableProfessors.size());
        Professor substitute = availableProfessors.get(professorId - 1);
        
        university.getTimetable().substituteProfessor(candidate, substitute);
        System.out.println("Done!");
        System.out.println(printer.printSchedules(Arrays.asList(candidate)));
    }

}
