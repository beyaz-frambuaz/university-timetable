package com.shablii.timetable.api.rest;

import com.shablii.timetable.api.AuditoriumsApi;
import com.shablii.timetable.api.rest.assemblers.AuditoriumModelAssembler;
import com.shablii.timetable.exceptions.*;
import com.shablii.timetable.model.Auditorium;
import com.shablii.timetable.service.TimetableFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequiredArgsConstructor
public class AuditoriumsController implements AuditoriumsApi {

    private final TimetableFacade timetableFacade;
    private final AuditoriumModelAssembler assembler;

    @Override
    public CollectionModel<EntityModel<Auditorium>> findAll() {

        List<EntityModel<Auditorium>> models = timetableFacade.getAuditoriums()
                .stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());
        return new CollectionModel<>(models, linkTo(methodOn(AuditoriumsApi.class).findAll()).withSelfRel());
    }

    @Override
    public ResponseEntity<EntityModel<Auditorium>> addNew(Auditorium auditorium) {

        EntityModel<Auditorium> entityModel = assembler.toModel(timetableFacade.saveAuditorium(auditorium));
        return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
    }

    @Override
    public EntityModel<Auditorium> findById(long id) {

        Auditorium auditorium = timetableFacade.getAuditorium(id)
                .orElseThrow(() -> new NotFoundException("Auditorium with ID " + id + " could not be found"));
        return assembler.toModel(auditorium);
    }

    @Override
    public EntityModel<Auditorium> editById(long id, Auditorium newAuditorium) {

        Auditorium savedAuditorium = timetableFacade.getAuditorium(id).map(auditorium -> {
            auditorium.setName(newAuditorium.getName());
            return timetableFacade.saveAuditorium(auditorium);
        }).orElseGet(() -> {
            newAuditorium.setId(id);
            return timetableFacade.saveAuditorium(newAuditorium);
        });
        return assembler.toModel(savedAuditorium);
    }

    @Override
    public ResponseEntity<Void> deleteById(long id) {

        throw new MethodNotImplementedException("auditoriums");
    }

}
