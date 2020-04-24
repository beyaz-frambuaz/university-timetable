package com.foxminded.timetable.menu;

import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.foxminded.timetable.model.Auditorium;
import com.foxminded.timetable.model.Course;
import com.foxminded.timetable.model.Period;
import com.foxminded.timetable.model.Professor;
import com.foxminded.timetable.model.ReschedulingOption;
import com.foxminded.timetable.model.Schedule;
import com.foxminded.timetable.model.ScheduleTemplate;
import com.foxminded.timetable.model.Student;
import com.foxminded.timetable.model.Timetable;
import com.foxminded.timetable.printer.Printer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuManager {

    private static final String MAIN_MENU = "\n1. Display two-week schedule\n"
            + "2. Display schedule\n" + "3. Display course attendees\n"
            + "4. Display available auditoriums\n"
            + "5. Display available professors\n" + "6. Reschedule course\n"
            + "7. Substitute professor\n" + "0. Exit\n";
    private static final String SCHEDULE_MODE = "\nPick schedule mode:\n"
            + "1. Day\n2. Week\n3. Month\n";
    private static final String SCHEDULE_TARGET = "\nSchedule for:\n"
            + "1. Student\n2. Professor\n3. Auditorium\n4. No filter\n";
    private static final Scanner SCANNER = new Scanner(System.in)
            .useDelimiter("\\n");

    private final Timetable timetable;
    private final Printer printer;
    private InputCollector inputCollector;

    @PostConstruct
    private void init() {
        this.inputCollector = new InputCollector(SCANNER);
    }

    public boolean loadMainMenu() {
        log.debug("Loading main menu");
        System.out.println(MAIN_MENU);
        int choice = inputCollector.requestOption(0, 7);

        switch (choice) {
        case 0:
            log.info("User chose to exit. Closing scanner and exiting...");
            SCANNER.close();
            System.out.println(
                    "Don't we all have more exciting things to do :)\nCheers!");
            return false;
        case 1:
            log.debug("User chose to view two week schedule");
            displayTwoWeekSchedule();
            break;
        case 2:
            log.debug("User chose to view schedule");
            displaySchedule();
            break;
        case 3:
            log.debug("User chose to view course attendees");
            displayCourseAttendees();
            break;
        case 4:
            log.debug("User chose to view available auditoriums");
            displayAvailableAuditoriums();
            break;
        case 5:
            log.debug("User chose to view available professors");
            displayAvailableProfessors();
            break;
        case 6:
            log.debug("User chose to reschedule course");
            rescheduleMenu();
            break;
        case 7:
            log.debug("User chose to substitute professor");
            substituteProfessor();
            break;
        default:
            break;
        }
        return true;
    }

    private List<Schedule> displayTwoWeekSchedule() {

        List<ScheduleTemplate> twoWeekSchedule = timetable.getTwoWeekSchedule();
        twoWeekSchedule.sort(Comparator.naturalOrder());
        System.out.println(printer.printTwoWeekSchedule(twoWeekSchedule));

        return Collections.emptyList();
    }

    private List<Schedule> displaySchedule() {

        System.out.println(SCHEDULE_MODE);
        int scheduleMode = inputCollector.requestOption(1, 3);
        switch (scheduleMode) {
        case 1:

            LocalDate date = inputCollector.requestDate(
                    timetable.getSemesterCalendar().getStartDate(),
                    timetable.getSemesterCalendar().getEndDate());
            log.debug("User chose to view schedule on {}", date);
            return displayScheduleForTarget(date, date);

        case 2:

            System.out.println("Enter week number: ");
            int weekNumber = inputCollector.requestOption(1,
                    timetable.getSemesterCalendar().getLengthInWeeks());
            LocalDate monday = timetable.getSemesterCalendar()
                    .getWeekMonday(weekNumber);
            LocalDate friday = timetable.getSemesterCalendar()
                    .getWeekFriday(weekNumber);
            log.debug("User chose to view schedule for week {}", weekNumber);
            return displayScheduleForTarget(monday, friday);

        case 3:

            System.out.println("Enter month number: ");
            int month = inputCollector.requestOption(
                    timetable.getSemesterCalendar().getStartDate()
                            .getMonthValue(),
                    timetable.getSemesterCalendar().getEndDate()
                            .getMonthValue());
            LocalDate firstOfMonth = timetable.getSemesterCalendar()
                    .getMonthStartDate(month);
            LocalDate lastOfMonth = timetable.getSemesterCalendar()
                    .getMonthEndDate(month);
            log.debug("User chose to view schedule for month {}", month);
            return displayScheduleForTarget(firstOfMonth, lastOfMonth);

        default:
            return Collections.emptyList();
        }
    }

    private List<Schedule> displayScheduleForTarget(LocalDate start,
            LocalDate end) {

        System.out.println(SCHEDULE_TARGET);
        int scheduleTarget = inputCollector.requestOption(1, 4);
        switch (scheduleTarget) {
        case 1:
            return displayStudentSchedule(start, end);
        case 2:
            return displayProfessorSchedule(start, end);
        case 3:
            return displayAuditoriumSchedule(start, end);
        case 4:

            log.debug("User chose to view schedule with no filters");
            List<Schedule> schedules = timetable.getRangeSchedule(start, end);
            System.out.println(printer.printSchedules(schedules));
            return schedules;

        default:
            return Collections.emptyList();
        }
    }

    private List<Schedule> displayStudentSchedule(LocalDate start,
            LocalDate end) {

        System.out.println("Pick a student ID: ");
        int studentId = inputCollector.requestOption(1,
                timetable.countStudents());
        log.debug("User chose to view schedule for student ID {}", studentId);
        List<Schedule> studentSchedule = timetable
                .findScheduleByStudentId(studentId, start, end);
        System.out.println(printer.printSchedules(studentSchedule));

        return studentSchedule;
    }

    private List<Schedule> displayProfessorSchedule(LocalDate start,
            LocalDate end) {

        List<Professor> professors = timetable.findProfessors();
        System.out.println(printer.printProfessors(professors));
        System.out.println("Pick a professor ID: ");
        List<Long> ids = professors.stream().map(Professor::getId)
                .collect(toList());
        long professorId = inputCollector.requestId(ids);
        log.debug("User chose to view schedule for professor ID {}",
                professorId);
        List<Schedule> professorSchedule = timetable
                .findScheduleByProfessorId(professorId, start, end);
        System.out.println(printer.printSchedules(professorSchedule));

        return professorSchedule;
    }

    private List<Schedule> displayAuditoriumSchedule(LocalDate start,
            LocalDate end) {

        List<Auditorium> auditoriums = timetable.findAuditoriums();
        System.out.println(printer.printAuditoriums(auditoriums));
        System.out.println("Pick an auditorium ID: ");
        List<Long> ids = auditoriums.stream().map(Auditorium::getId)
                .collect(toList());
        long auditoriumId = inputCollector.requestId(ids);
        log.debug("User chose to view schedule for auditorium ID {}",
                auditoriumId);
        List<Schedule> auditoriumSchedule = timetable
                .findScheduleByAuditoriumId(auditoriumId, start, end);
        System.out.println(printer.printSchedules(auditoriumSchedule));

        return auditoriumSchedule;
    }

    private void displayCourseAttendees() {

        List<Professor> professors = timetable.findProfessors();
        System.out.println(printer.printProfessors(professors));
        System.out.println("Pick a professor ID: ");
        List<Long> ids = professors.stream().map(Professor::getId)
                .collect(toList());
        long professorId = inputCollector.requestId(ids);
        log.debug("User chose professor ID {}", professorId);
        Professor professor = professors.stream()
                .filter(p -> p.getId() == professorId).findFirst().get();

        List<Course> courses = professor.getCourses();
        System.out.println(printer.printCourses(courses));
        System.out.println("Pick a course:");
        List<Long> courseIds = courses.stream().map(Course::getId)
                .collect(toList());
        long courseId = inputCollector.requestId(courseIds);
        log.debug("User chose course ID {}", courseId);
        List<Student> attendees = timetable.findCourseAttendees(courseId,
                professorId);

        System.out.println(printer.printStudents(attendees));
    }

    private void displayAvailableAuditoriums() {

        LocalDate date = inputCollector.requestDate(
                timetable.getSemesterCalendar().getStartDate(),
                timetable.getSemesterCalendar().getEndDate());
        System.out.println(printer.printPeriods());
        int periodId = inputCollector.requestOption(1, Period.values().length);
        Period period = Period.values()[periodId - 1];
        log.debug("User chose period {} on {}", period, date);
        List<Auditorium> availableAuditoriums = timetable
                .findAvailableAuditoriums(date, period);
        System.out.println(printer.printAuditoriums(availableAuditoriums));
    }

    private void displayAvailableProfessors() {

        LocalDate date = inputCollector.requestDate(
                timetable.getSemesterCalendar().getStartDate(),
                timetable.getSemesterCalendar().getEndDate());
        System.out.println(printer.printPeriods());
        int periodId = inputCollector.requestOption(1, Period.values().length);
        Period period = Period.values()[periodId - 1];
        log.debug("User chose period {} on {}", period, date);
        List<Professor> availableProfessors = timetable
                .findAvailableProfessors(date, period);
        System.out.println(printer.printProfessors(availableProfessors));
    }

    private void rescheduleMenu() {

        List<Schedule> schedules = displaySchedule();
        List<Long> ids = schedules.stream().map(Schedule::getId)
                .collect(toList());
        System.out.println("Pick a schedule ID: ");
        long scheduleId = inputCollector.requestId(ids);
        log.debug("User chose to reschedule schedule ID {}", scheduleId);
        Schedule candidate = schedules.stream()
                .filter(s -> s.getId() == scheduleId).findFirst().get();

        System.out.println("\nSee rescheduling options for:\n1. Date\n"
                + "2. Week\n3. Cancel\n");
        int choice = inputCollector.requestOption(1, 3);
        switch (choice) {
        case 1:

            LocalDate date = inputCollector.requestDate(
                    timetable.getSemesterCalendar().getStartDate(),
                    timetable.getSemesterCalendar().getEndDate());
            log.debug("User chose to view rescheduling options on {}", date);

            selectReschedulingOption(candidate, date, date);
            break;

        case 2:

            System.out.println("Enter week number: ");
            int week = inputCollector.requestOption(1,
                    timetable.getSemesterCalendar().getLengthInWeeks());
            LocalDate monday = timetable.getSemesterCalendar()
                    .getWeekMonday(week);
            LocalDate friday = timetable.getSemesterCalendar()
                    .getWeekFriday(week);
            log.debug("User chose to view rescheduling options for week {}",
                    week);

            selectReschedulingOption(candidate, monday, friday);
            break;

        case 3:
            log.debug("User chose to cancel and go back to main menu");
            break;
        default:
            break;
        }
    }

    private void selectReschedulingOption(Schedule candidate,
            LocalDate startDate, LocalDate endDate) {

        Map<LocalDate, List<ReschedulingOption>> options = timetable
                .getReschedulingOptions(candidate, startDate, endDate);
        System.out.println(printer.printReschedulingOptions(options));
        LocalDate targetDate = inputCollector.requestDate(startDate, endDate);
        int optionId = inputCollector.requestOption(1,
                options.get(targetDate).size());
        ReschedulingOption option = options.get(targetDate).get(optionId - 1);
        log.debug("User chose rescheduling option: {}", option);
        rescheduleByOption(candidate, targetDate, option);
    }

    private void rescheduleByOption(Schedule candidate, LocalDate targetDate,
            ReschedulingOption targetOption) {

        System.out.println("Reschedule:\n1. Once\n2. Permanently\n3. Cancel\n");
        int choice = inputCollector.requestOption(1, 3);
        switch (choice) {
        case 1:

            log.debug("User chose to reschedule once");
            candidate = timetable.rescheduleOnce(candidate, targetDate,
                    targetOption);
            System.out.println(
                    "Done!\nHere is how this schedule looks like now:");
            System.out
                    .println(printer.printSchedules(Arrays.asList(candidate)));
            break;

        case 2:

            log.debug("User chose to reschedule permanently");
            List<Schedule> affectedSchedules = timetable
                    .reschedulePermanently(candidate, targetDate, targetOption);
            System.out.println("Done!\nThe following items were affected:");
            System.out.println(printer.printSchedules(affectedSchedules));
            break;

        case 3:
            log.debug("User chose to cancel and go back to main menu");
            break;
        default:
            break;
        }
    }

    private void substituteProfessor() {

        List<Schedule> schedules = displaySchedule();
        List<Long> ids = schedules.stream().map(Schedule::getId)
                .collect(toList());
        System.out.println("Pick a schedule ID: ");
        long scheduleId = inputCollector.requestId(ids);
        Schedule schedule = schedules.stream()
                .filter(s -> s.getId() == scheduleId).findFirst().get();

        List<Professor> availableProfessors = timetable.findAvailableProfessors(
                schedule.getDate(), schedule.getPeriod());
        System.out.println("Available professors:");
        System.out.println(printer.printProfessors(availableProfessors));
        List<Long> professorIds = availableProfessors.stream()
                .map(Professor::getId).collect(toList());
        System.out.println("Pick a professor ID: ");
        long professorId = inputCollector.requestId(professorIds);
        log.debug(
                "User chose to substitute professor ID {} with professor ID {} for schedule ID {}",
                schedule.getProfessor().getId(), professorId, schedule.getId());

        schedule = timetable.substituteProfessor(scheduleId, professorId);
        System.out.println("Done!\nHere is how this schedule looks like now:");
        System.out.println(printer.printSchedules(Arrays.asList(schedule)));
    }

}
