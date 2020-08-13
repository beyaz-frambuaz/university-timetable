package com.foxminded.timetable.api.rest;

import com.foxminded.timetable.api.TemplatesApi;
import com.foxminded.timetable.api.rest.assemblers.TemplateModelAssembler;
import com.foxminded.timetable.exceptions.NotFoundException;
import com.foxminded.timetable.model.ScheduleTemplate;
import com.foxminded.timetable.service.TimetableFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.*;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequiredArgsConstructor
public class TemplatesController implements TemplatesApi {

    private final TimetableFacade timetableFacade;
    private final TemplateModelAssembler assembler;

    @Override
    public CollectionModel<EntityModel<ScheduleTemplate>> findAll() {

        List<EntityModel<ScheduleTemplate>> models =
                timetableFacade.getTwoWeekSchedule()
                        .stream()
                        .map(assembler::toModel)
                        .collect(Collectors.toList());
        return new CollectionModel<>(models,
                linkTo(methodOn(TemplatesApi.class).findAll()).withSelfRel());
    }

    @Override
    public EntityModel<ScheduleTemplate> findById(long id) {

        ScheduleTemplate template = timetableFacade.getTemplate(id)
                .orElseThrow(() -> new NotFoundException(
                        "Template with ID " + id + " could not be found"));
        return assembler.toModel(template);
    }

}
