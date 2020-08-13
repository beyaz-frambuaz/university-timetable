package com.foxminded.timetable.api.rest;

import com.foxminded.timetable.api.SemesterApi;
import com.foxminded.timetable.service.utility.SemesterCalendar;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequiredArgsConstructor
public class SemesterController implements SemesterApi {

    private final SemesterCalendar semesterCalendar;

    @Override
    public EntityModel<SemesterCalendar.Semester> getSemester() {

        return new EntityModel<>(semesterCalendar.getSemester(),
                linkTo(methodOn(
                        SemesterApi.class).getSemester()).withSelfRel());
    }

}
