package com.foxminded.timetable.web;

import com.foxminded.timetable.model.Period;
import com.foxminded.timetable.model.Schedule;
import com.foxminded.timetable.model.ScheduleTemplate;
import com.foxminded.timetable.model.Student;
import com.foxminded.timetable.service.SemesterCalendarUtils;
import com.foxminded.timetable.service.StudentService;
import com.foxminded.timetable.service.TimetableService;
import com.foxminded.timetable.web.utils.MonthScheduleView;
import com.foxminded.timetable.web.utils.WeekScheduleView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/students")
public class StudentController {

    private final StudentService        service;
    private final TimetableService      timetableService;
    private final SemesterCalendarUtils semesterCalendar;

    @GetMapping("/student_select")
    public String selectStudent(Model model) {

        List<Student> students = service.findAll();

        model.addAttribute("students", students);

        return "student_select";
    }

    @GetMapping("/student_home")
    public String studentHome(@RequestParam("studentId") long studentId,
            Model model) {

        Optional<Student> studentOptional = service.findById(studentId);
        if (!studentOptional.isPresent()) {
            return "404";
        }
        Student student = studentOptional.get();
        model.addAttribute("student", student);

        LocalDate today = LocalDate.now();
        model.addAttribute("today", today.format(
                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)));

        Map<Period, List<Schedule>> daySchedule =
                timetableService.getStudentSchedule(
                student, today, today)
                .stream()
                .collect(Collectors.groupingBy(Schedule::getPeriod,
                        LinkedHashMap::new, Collectors.toList()));
        model.addAttribute("daySchedule", daySchedule);

        return "student_home";
    }

    @GetMapping("/student_two_week")
    public String studentTwoWeekSchedule(
            @RequestParam("studentId") long studentId, Model model) {

        Optional<Student> studentOptional = service.findById(studentId);
        if (!studentOptional.isPresent()) {
            return "404";
        }
        Student student = studentOptional.get();
        model.addAttribute("student", student);

        List<ScheduleTemplate> twoWeek = timetableService.getTwoWeekSchedule();

        Map<Period, List<ScheduleTemplate>> oddWeek = twoWeek.stream()
                .filter(t -> !t.getWeekParity())
                .sorted()
                .collect(Collectors.groupingBy(ScheduleTemplate::getPeriod,
                        LinkedHashMap::new, Collectors.toList()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(
                        Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                (e1, e2) -> e1, LinkedHashMap::new));

        Map<Period, List<ScheduleTemplate>> evenWeek = twoWeek.stream()
                .filter(ScheduleTemplate::getWeekParity)
                .sorted()
                .collect(Collectors.groupingBy(ScheduleTemplate::getPeriod,
                        LinkedHashMap::new, Collectors.toList()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(
                        Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                (e1, e2) -> e1, LinkedHashMap::new));

        model.addAttribute("oddWeek", oddWeek);
        model.addAttribute("evenWeek", evenWeek);

        return "student_two_week";
    }

    @PostMapping("/schedule")
    public String processForm(@RequestParam("studentId") long studentId,
            @RequestParam("scheduleOption") String scheduleOption,
            @RequestParam("date") String inputDate,
            @RequestParam(value = "filterSchedule",
                          required = false) String filterSchedule,
            Model model) {

        Optional<Student> studentOptional = service.findById(studentId);
        if (!studentOptional.isPresent()) {
            return "404";
        }
        Student student = studentOptional.get();
        model.addAttribute("student", student);

        LocalDate date = LocalDate.parse(inputDate);
        model.addAttribute("date", date.format(
                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)));

        if (scheduleOption.equals("date")) {
            List<Schedule> schedule;
            if (filterSchedule == null) {
                schedule = timetableService.getScheduleInRange(date, date);
                model.addAttribute("filtered", false);
            } else {
                schedule = timetableService.getStudentSchedule(student, date,
                        date);
                model.addAttribute("filtered", true);
            }
            model.addAttribute("daySchedule",
                    convertToPeriodSchedule(schedule));

            return "student_day_schedule";
        }

        if (scheduleOption.equals("week")) {

            LocalDate monday = semesterCalendar.getWeekMonday(date);
            LocalDate friday = semesterCalendar.getWeekFriday(date);
            List<Schedule> schedule;
            boolean filtered;
            if (filterSchedule == null) {
                schedule = timetableService.getScheduleInRange(monday, friday);
                filtered = false;
            } else {
                schedule = timetableService.getStudentSchedule(student, monday,
                        friday);
                filtered = true;
            }

            model.addAttribute("weekSchedule",
                    prepareWeekSchedule(monday, filtered, schedule));

            return "student_week_schedule";
        }

        if (scheduleOption.equals("month")) {

            LocalDate firstOfMonth = semesterCalendar.getFirstSemesterMondayOfMonth(date);
            LocalDate lastOfMonth = semesterCalendar.getLastSemesterFridayOfMonth(date);
            List<Schedule> schedule;
            boolean filtered;
            if (filterSchedule == null) {
                schedule = timetableService.getScheduleInRange(firstOfMonth,
                        lastOfMonth);
                filtered = false;
            } else {
                schedule = timetableService.getStudentSchedule(student,
                        firstOfMonth, lastOfMonth);
                filtered = true;
            }

            model.addAttribute("monthSchedule",
                    prepareMonthSchedule(firstOfMonth, lastOfMonth, filtered,
                            schedule));

            return "student_month_schedule";
        }


        return "404";
    }

    private MonthScheduleView prepareMonthSchedule(LocalDate firstOfMonth,
            LocalDate lastOfMonth, boolean filtered, List<Schedule> schedules) {

        String monthDescription = semesterCalendar.getMonthDescription(
                firstOfMonth);

        List<WeekScheduleView> weekScheduleViews = new ArrayList<>();
        int start = semesterCalendar.getSemesterWeekNumber(firstOfMonth);
        int end = semesterCalendar.getSemesterWeekNumber(lastOfMonth);
        for (; start <= end; start++) {
            LocalDate monday = semesterCalendar.getWeekMonday(start);
            int finalStart = start;
            List<Schedule> weekSchedules = schedules.stream()
                    .filter(schedule -> semesterCalendar.getSemesterWeekNumber(
                            schedule.getDate()) == finalStart)
                    .collect(Collectors.toList());
            weekScheduleViews.add(
                    prepareWeekSchedule(monday, filtered, weekSchedules));
        }


        return new MonthScheduleView(weekScheduleViews, filtered,
                monthDescription);
    }

    private Map<Period, List<Schedule>> convertToPeriodSchedule(
            List<Schedule> schedule) {

        return schedule.stream()
                .collect(Collectors.groupingBy(Schedule::getPeriod,
                        LinkedHashMap::new, Collectors.toList()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(
                        Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                (e1, e2) -> e1, LinkedHashMap::new));
    }

    private WeekScheduleView prepareWeekSchedule(LocalDate monday,
            boolean filtered, List<Schedule> schedules) {

        String weekDescription = semesterCalendar.getWeekDescription(monday);
        int weekNumber = semesterCalendar.getSemesterWeekNumber(monday);
        String mon = monday.format(
                DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT));
        String tue = monday.plusDays(1L)
                .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT));
        String wed = monday.plusDays(2L)
                .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT));
        String thu = monday.plusDays(3L)
                .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT));
        String fri = monday.plusDays(4L)
                .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT));

        return new WeekScheduleView(convertToPeriodSchedule(schedules),
                filtered, weekDescription, weekNumber, mon, tue, wed, thu, fri);
    }

}
