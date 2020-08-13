package com.foxminded.timetable.api.rest;

import com.foxminded.timetable.api.GroupsApi;
import com.foxminded.timetable.api.rest.assemblers.GroupModelAssembler;
import com.foxminded.timetable.exceptions.*;
import com.foxminded.timetable.model.Group;
import com.foxminded.timetable.service.TimetableFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequiredArgsConstructor
public class GroupsController implements GroupsApi {

    private final TimetableFacade timetableFacade;
    private final GroupModelAssembler assembler;

    @Override
    public CollectionModel<EntityModel<Group>> findAll() {

        List<EntityModel<Group>> models = timetableFacade.getGroups()
                .stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());
        return new CollectionModel<>(models,
                linkTo(methodOn(GroupsApi.class).findAll()).withSelfRel());
    }

    @Override
    public ResponseEntity<EntityModel<Group>> addNew(Group group) {

        EntityModel<Group> entityModel =
                assembler.toModel(timetableFacade.saveGroup(group));
        return ResponseEntity.created(
                entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @Override
    public EntityModel<Group> findById(long id) {

        Group group = timetableFacade.getGroup(id)
                .orElseThrow(() -> new NotFoundException(
                        "Group with ID " + id + " could not be found"));
        return assembler.toModel(group);
    }

    @Override
    public EntityModel<Group> editById(long id, Group newGroup) {

        Group savedGroup = timetableFacade.getGroup(id).map(group -> {
            group.setName(newGroup.getName());
            return timetableFacade.saveGroup(group);
        }).orElseGet(() -> {
            newGroup.setId(id);
            return timetableFacade.saveGroup(newGroup);
        });
        return assembler.toModel(savedGroup);
    }

    @Override
    public ResponseEntity<Void> deleteById(long id) {

        throw new MethodNotImplementedException("groups");
    }

}
