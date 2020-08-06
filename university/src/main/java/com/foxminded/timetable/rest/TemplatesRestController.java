package com.foxminded.timetable.rest;

import com.foxminded.timetable.constraints.IdValid;
import com.foxminded.timetable.exceptions.NotFoundException;
import com.foxminded.timetable.model.ScheduleTemplate;
import com.foxminded.timetable.rest.assemblers.TemplateModelAssembler;
import com.foxminded.timetable.service.TimetableFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/v1/timetable/templates")
@Validated
@RequiredArgsConstructor
public class TemplatesRestController {

    private final TimetableFacade timetableFacade;
    private final TemplateModelAssembler assembler;

    @GetMapping
    public CollectionModel<EntityModel<ScheduleTemplate>> findAll() {

        List<EntityModel<ScheduleTemplate>> models =
                timetableFacade.getTwoWeekSchedule()
                        .stream()
                        .map(assembler::toModel)
                        .collect(Collectors.toList());
        return new CollectionModel<>(models, linkTo(methodOn(
                TemplatesRestController.class).findAll()).withSelfRel());
    }

    @GetMapping("/{id}")
    public EntityModel<ScheduleTemplate> findById(
            @PathVariable @IdValid("Template") long id) {

        ScheduleTemplate template = timetableFacade.getTemplate(id)
                .orElseThrow(() -> new NotFoundException(
                        "Template with ID " + id + " could not be found"));
        return assembler.toModel(template);
    }

}
