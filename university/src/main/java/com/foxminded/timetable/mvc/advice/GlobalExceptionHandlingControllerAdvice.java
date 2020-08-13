package com.foxminded.timetable.mvc.advice;

import com.foxminded.timetable.exceptions.SessionExpiredException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandlingControllerAdvice {

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


