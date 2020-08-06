package com.foxminded.timetable.rest.assemblers;

import com.foxminded.timetable.model.Schedule;
import com.foxminded.timetable.rest.SchedulesRestController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class ScheduleModelAssembler implements
        RepresentationModelAssembler<Schedule, EntityModel<Schedule>> {

    @Override
    public EntityModel<Schedule> toModel(Schedule schedule) {

        SchedulesRestController controller =
                methodOn(SchedulesRestController.class);

        return new EntityModel<>(schedule,
                linkTo(controller.findById(schedule.getId())).withSelfRel(),
                linkTo(controller.findAvailableAuditoriums(
                        schedule.getId())).withRel("available auditoriums"),
                linkTo(controller.findAvailableProfessors(
                        schedule.getId())).withRel("available professors"),
                linkTo(controller.findOptions(schedule.getId(), null,
                        null)).withRel("rescheduling options"),
                linkTo(controller.reschedule(schedule.getId(), schedule, false))
                        .withRel("reschedule"));
    }

}
