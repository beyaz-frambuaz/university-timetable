package com.foxminded.timetable.api.rest.assemblers;

import com.foxminded.timetable.api.TemplatesApi;
import com.foxminded.timetable.model.ScheduleTemplate;
import com.foxminded.timetable.api.rest.*;
import org.springframework.hateoas.*;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class TemplateModelAssembler implements
        RepresentationModelAssembler<ScheduleTemplate,
                EntityModel<ScheduleTemplate>> {

    @Override
    public EntityModel<ScheduleTemplate> toModel(ScheduleTemplate template) {

        TemplatesApi controller =
                methodOn(TemplatesController.class);

        return new EntityModel<>(template,
                linkTo(controller.findById(template.getId())).withSelfRel(),
                linkTo(controller.findAll()).withRel(
                        IanaLinkRelations.COLLECTION));
    }

}
