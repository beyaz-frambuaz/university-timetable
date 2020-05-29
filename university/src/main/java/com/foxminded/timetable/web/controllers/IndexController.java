package com.foxminded.timetable.web.controllers;

import com.foxminded.timetable.service.model.generator.TimetableModelGenerator;
import com.foxminded.timetable.service.model.generator.UniversityModelGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

@Controller
@RequiredArgsConstructor
@RequestMapping({ "/", "/index" })
public class IndexController {

    private final UniversityModelGenerator universityModelGenerator;
    private final TimetableModelGenerator  timetableModelGenerator;

    @GetMapping
    public String index(HttpSession session) {

        session.removeAttribute("student");
        session.removeAttribute("professor");
        return "index";
    }

}
