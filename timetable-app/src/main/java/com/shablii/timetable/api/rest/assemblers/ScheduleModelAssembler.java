package com.shablii.timetable.api.rest.assemblers;

import com.shablii.timetable.api.SchedulesApi;
import com.shablii.timetable.api.rest.SchedulesController;
import com.shablii.timetable.model.Schedule;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class ScheduleModelAssembler implements RepresentationModelAssembler<Schedule, EntityModel<Schedule>> {

    @Override
    public EntityModel<Schedule> toModel(Schedule schedule) {

        SchedulesApi controller = methodOn(SchedulesController.class);

        return new EntityModel<>(schedule, linkTo(controller.findById(schedule.getId())).withSelfRel(),
                linkTo(controller.findAvailableAuditoriums(schedule.getId())).withRel("available auditoriums"),
                linkTo(controller.findAvailableProfessors(schedule.getId())).withRel("available professors"),
                linkTo(controller.findOptions(schedule.getId(), null, null)).withRel("rescheduling options"),
                linkTo(controller.reschedule(schedule.getId(), schedule, false)).withRel("reschedule"));
    }

}
