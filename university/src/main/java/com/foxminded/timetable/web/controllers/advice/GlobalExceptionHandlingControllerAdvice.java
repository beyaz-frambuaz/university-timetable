package com.foxminded.timetable.web.controllers.advice;

import com.foxminded.timetable.web.exception.NotFoundException;
import com.foxminded.timetable.web.exception.SessionExpiredException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandlingControllerAdvice {

    @ExceptionHandler(NotFoundException.class)
    public ModelAndView handleNotFound(HttpServletRequest req,
            NotFoundException exception) {

        log.error("Request: " + req.getRequestURI() + req.getQueryString()
                + "raised " + exception);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("message", exception.getMessage());
        modelAndView.setViewName("error/404");
        return modelAndView;
    }

    @ExceptionHandler(SessionExpiredException.class)
    public String handleSessionExpired(SessionExpiredException exception,
            RedirectAttributes redirectAttributes) {

        String sessionExpired;
        switch (exception.getContext().getSimpleName()) {

            case "Student":
                log.error("Student session expired, redirecting to list");
                sessionExpired = "Session seems to have expired, please "
                        + "select a student to view schedule";
                redirectAttributes.addFlashAttribute("sessionExpired",
                        sessionExpired);
                return "redirect:/timetable/students/list";
            case "Professor":
                log.error("Professor session expired, redirecting to list");
                sessionExpired = "Session seems to have expired, please "
                        + "select a professor to view schedule";
                redirectAttributes.addFlashAttribute("sessionExpired",
                        sessionExpired);
                return "redirect:/timetable/faculty/list";
            default:
                return "error";

        }
    }

}


