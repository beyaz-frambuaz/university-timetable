package com.foxminded.timetable.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping({ "/", "/index" })
public class IndexController {

    @GetMapping
    public String index(HttpSession session) {

        session.removeAttribute("student");
        session.removeAttribute("professor");
        return "index";
    }

}
