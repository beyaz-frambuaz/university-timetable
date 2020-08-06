package com.foxminded.timetable.rest.assemblers;

import com.foxminded.timetable.model.ScheduleTemplate;
import com.foxminded.timetable.rest.*;
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

        TemplatesRestController controller =
                methodOn(TemplatesRestController.class);

        return new EntityModel<>(template,
                linkTo(controller.findById(template.getId())).withSelfRel(),
                linkTo(controller.findAll()).withRel(
                        IanaLinkRelations.COLLECTION));
    }

}
