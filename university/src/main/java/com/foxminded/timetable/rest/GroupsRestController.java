package com.foxminded.timetable.rest;

import com.foxminded.timetable.constraints.IdValid;
import com.foxminded.timetable.exceptions.*;
import com.foxminded.timetable.model.Group;
import com.foxminded.timetable.rest.assemblers.GroupModelAssembler;
import com.foxminded.timetable.service.TimetableFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/v1/timetable/groups")
@Validated
@RequiredArgsConstructor
public class GroupsRestController {

    private final TimetableFacade timetableFacade;
    private final GroupModelAssembler assembler;

    @GetMapping
    public CollectionModel<EntityModel<Group>> findAll() {

        List<EntityModel<Group>> models = timetableFacade.getGroups()
                .stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());
        return new CollectionModel<>(models, linkTo(methodOn(
                GroupsRestController.class).findAll()).withSelfRel());
    }

    @PostMapping
    public ResponseEntity<EntityModel<Group>> addNew(
            @RequestBody @Valid Group group) {

        EntityModel<Group> entityModel =
                assembler.toModel(timetableFacade.saveGroup(group));
        return ResponseEntity.created(
                entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @GetMapping("/{id}")
    public EntityModel<Group> findById(
            @PathVariable @IdValid("Group") long id) {

        Group group = timetableFacade.getGroup(id)
                .orElseThrow(() -> new NotFoundException(
                        "Group with ID " + id + " could not be found"));
        return assembler.toModel(group);
    }

    @PutMapping("/{id}")
    public EntityModel<Group> editById(@PathVariable @IdValid("Group") long id,
            @RequestBody @Valid Group newGroup) {

        Group savedGroup = timetableFacade.getGroup(id).map(group -> {
            group.setName(newGroup.getName());
            return timetableFacade.saveGroup(group);
        }).orElseGet(() -> {
            newGroup.setId(id);
            return timetableFacade.saveGroup(newGroup);
        });
        return assembler.toModel(savedGroup);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(
            @PathVariable @IdValid("Group") long id) {

        throw new MethodNotImplementedException("groups");
    }

}
