package com.foxminded.university.timetable;

import java.time.LocalDate;

import com.foxminded.university.timetable.menu.MenuManager;
import com.foxminded.university.timetable.model.SemesterProperties;
import com.foxminded.university.timetable.model.Timetable;
import com.foxminded.university.timetable.model.University;
import com.foxminded.university.timetable.model.generator.TimetableModelGenerator;
import com.foxminded.university.timetable.model.generator.UniversityModelGenerator;
import com.foxminded.university.timetable.printer.Printer;
import com.foxminded.university.timetable.printer.assembler.Assembler;

public class UniversityTimetableApplication {
    public static void main(String[] args) {
        String firstNamesFilePath = "first_names.txt";
        String lastNamesFilePath = "last_names.txt";
        String coursesFilePath = "courses.txt";

        UniversityModelGenerator universityModelGenerator = new UniversityModelGenerator();
        University university = universityModelGenerator
                .generateUniversity(firstNamesFilePath, lastNamesFilePath,
                        coursesFilePath);
        LocalDate semesterStartDate = LocalDate.of(2020, 9, 7);
        LocalDate semesterEndDate = LocalDate.of(2020, 12, 11);
        SemesterProperties semesterProperties = new SemesterProperties(
                semesterStartDate, semesterEndDate);
        university.setSemesterProperties(semesterProperties);

        TimetableModelGenerator timetableModelGenerator = new TimetableModelGenerator(
                university);
        Timetable timetable = timetableModelGenerator.generateTimetable();
        university.setTimetable(timetable);
        
        Assembler assembler = new Assembler();
        Printer printer = new Printer(assembler);
        MenuManager manager = new MenuManager(university, printer);

        boolean keepRunning = true;
        
        while (keepRunning) {
            keepRunning = manager.loadMainMenu();
        }
    }
}
