package com.foxminded.timetable.rest;

import com.foxminded.timetable.service.utility.SemesterCalendar;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/v1/timetable/semester")
@Validated
@RequiredArgsConstructor
public class SemesterRestController {

    private final SemesterCalendar semesterCalendar;

    @GetMapping
    public EntityModel<SemesterCalendar.Semester> getSemester() {

        return new EntityModel<>(semesterCalendar.getSemester(),
                linkTo(methodOn(
                        SemesterRestController.class).getSemester()).withSelfRel());
    }

}
