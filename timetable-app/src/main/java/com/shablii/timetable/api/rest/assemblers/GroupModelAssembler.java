package com.shablii.timetable.api.rest.assemblers;

import com.shablii.timetable.api.GroupsApi;
import com.shablii.timetable.api.rest.GroupsController;
import com.shablii.timetable.model.Group;
import org.springframework.hateoas.*;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class GroupModelAssembler implements RepresentationModelAssembler<Group, EntityModel<Group>> {

    @Override
    public EntityModel<Group> toModel(Group group) {

        GroupsApi controller = methodOn(GroupsController.class);

        return new EntityModel<>(group, linkTo(controller.findById(group.getId())).withSelfRel(),
                linkTo(controller.findAll()).withRel(IanaLinkRelations.COLLECTION));
    }

}
