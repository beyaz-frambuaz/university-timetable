package com.shablii.timetable.api;

import com.shablii.timetable.exceptions.ApiException;
import com.shablii.timetable.model.Auditorium;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;

@RequestMapping("/api/v1/timetable/auditoriums")
@Validated
@Tag(name = "auditoriums", description = "Auditoriums API")
public interface AuditoriumsApi {

    @GetMapping(produces = { "application/json" })
    @Operation(summary = "List all auditoriums", tags = { "auditorium" })
    @ApiResponse(responseCode = "200", description = "operation successful",
                 content = @Content(array = @ArraySchema(schema = @Schema(implementation = Auditorium.class))))
    CollectionModel<EntityModel<Auditorium>> findAll();

    @PostMapping(consumes = { "application/json" }, produces = { "application/json" })
    @Operation(summary = "Create new auditorium", tags = { "auditorium" })
    @ApiResponse(responseCode = "201", description = "auditorium created",
                 content = @Content(schema = @Schema(implementation = Auditorium.class)))
    @ApiResponse(responseCode = "400", description = "invalid data submitted",
                 content = @Content(schema = @Schema(implementation = ApiException.class)))
    ResponseEntity<EntityModel<Auditorium>> addNew(@RequestBody @Valid Auditorium auditorium);

    @GetMapping(path = "/{id}", produces = { "application/json" })
    @Operation(summary = "Find auditorium by ID", tags = { "auditorium" })
    @ApiResponse(responseCode = "200", description = "found",
                 content = @Content(schema = @Schema(implementation = Auditorium.class)))
    @ApiResponse(responseCode = "400", description = "invalid ID submitted",
                 content = @Content(schema = @Schema(implementation = ApiException.class)))
    @ApiResponse(responseCode = "404", description = "auditorium not found",
                 content = @Content(schema = @Schema(implementation = ApiException.class)))
    EntityModel<Auditorium> findById(@PathVariable @Min(1) long id);

    @PutMapping(path = "/{id}", consumes = { "application/json" }, produces = { "application/json" })
    @Operation(summary = "Edit auditorium with ID", tags = { "auditorium" },
               description = "Either updates existing or saves new " + "auditorium, returns result")
    @ApiResponse(responseCode = "200", description = "operation successful",
                 content = @Content(schema = @Schema(implementation = Auditorium.class)))
    @ApiResponse(responseCode = "400", description = "invalid data submitted",
                 content = @Content(schema = @Schema(implementation = ApiException.class)))
    EntityModel<Auditorium> editById(@PathVariable @Min(1) long id, @RequestBody @Valid Auditorium newAuditorium);

    @DeleteMapping(path = "/{id}", produces = { "application/json" })
    @Operation(summary = "Delete auditorium by ID", tags = { "auditorium" })
    @ApiResponse(responseCode = "400", description = "invalid ID submitted",
                 content = @Content(schema = @Schema(implementation = ApiException.class)))
    @ApiResponse(responseCode = "501", description = "not yet implemented",
                 content = @Content(schema = @Schema(implementation = ApiException.class)))
    ResponseEntity<Void> deleteById(@PathVariable @Min(1) long id);

}
