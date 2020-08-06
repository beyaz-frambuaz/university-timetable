package com.foxminded.timetable.rest;

import com.foxminded.timetable.constraints.IdValid;
import com.foxminded.timetable.exceptions.*;
import com.foxminded.timetable.model.Auditorium;
import com.foxminded.timetable.rest.assemblers.AuditoriumModelAssembler;
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
@RequestMapping("/api/v1/timetable/auditoriums")
@Validated
@RequiredArgsConstructor
public class AuditoriumsRestController {

    private final TimetableFacade timetableFacade;
    private final AuditoriumModelAssembler assembler;

    @GetMapping
    public CollectionModel<EntityModel<Auditorium>> findAll() {

        List<EntityModel<Auditorium>> models = timetableFacade.getAuditoriums()
                .stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());
        return new CollectionModel<>(models, linkTo(methodOn(
                AuditoriumsRestController.class).findAll()).withSelfRel());
    }

    @PostMapping
    public ResponseEntity<EntityModel<Auditorium>> addNew(
            @RequestBody @Valid Auditorium auditorium) {

        EntityModel<Auditorium> entityModel =
                assembler.toModel(timetableFacade.saveAuditorium(auditorium));
        return ResponseEntity.created(
                entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @GetMapping("/{id}")
    public EntityModel<Auditorium> findById(
            @PathVariable @IdValid("Auditorium") long id) {

        Auditorium auditorium = timetableFacade.getAuditorium(id)
                .orElseThrow(() -> new NotFoundException(
                        "Auditorium with ID " + id + " could not be found"));
        return assembler.toModel(auditorium);
    }

    @PutMapping("/{id}")
    public EntityModel<Auditorium> editById(
            @PathVariable @IdValid("Auditorium") long id,
            @RequestBody @Valid Auditorium newAuditorium) {

        Auditorium savedAuditorium =
                timetableFacade.getAuditorium(id).map(auditorium -> {
                    auditorium.setName(newAuditorium.getName());
                    return timetableFacade.saveAuditorium(auditorium);
                }).orElseGet(() -> {
                    newAuditorium.setId(id);
                    return timetableFacade.saveAuditorium(newAuditorium);
                });
        return assembler.toModel(savedAuditorium);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(
            @PathVariable @IdValid("Auditorium") long id) {

        throw new MethodNotImplementedException("auditoriums");
    }

}
