package com.foxminded.timetable.api;

import com.foxminded.timetable.exceptions.ApiException;
import com.foxminded.timetable.model.Group;
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

@RequestMapping("/api/v1/timetable/groups")
@Validated
@Tag(name = "groups", description = "Groups API")
public interface GroupsApi {

    @GetMapping(produces = { "application/json" })
    @Operation(summary = "List all groups", tags = { "groups" })
    @ApiResponse(responseCode = "200", description = "operation successful",
                 content = @Content(array = @ArraySchema(
                         schema = @Schema(implementation = Group.class))))
    CollectionModel<EntityModel<Group>> findAll();

    @PostMapping(consumes = { "application/json" },
                 produces = { "application/json" })
    @Operation(summary = "Create new group", tags = { "groups" })
    @ApiResponse(responseCode = "201", description = "group created",
                 content = @Content(
                         schema = @Schema(implementation = Group.class)))
    @ApiResponse(responseCode = "400", description = "invalid data submitted",
                 content = @Content(
                         schema = @Schema(implementation = ApiException.class)))
    ResponseEntity<EntityModel<Group>> addNew(@RequestBody @Valid Group group);

    @GetMapping(path = "/{id}", produces = { "application/json" })
    @Operation(summary = "Find group by ID", tags = { "groups" })
    @ApiResponse(responseCode = "200", description = "found",
                 content = @Content(
                         schema = @Schema(implementation = Group.class)))
    @ApiResponse(responseCode = "400", description = "invalid ID submitted",
                 content = @Content(
                         schema = @Schema(implementation = ApiException.class)))
    @ApiResponse(responseCode = "404", description = "group not found",
                 content = @Content(
                         schema = @Schema(implementation = ApiException.class)))
    EntityModel<Group> findById(@PathVariable @Min(1) long id);

    @PutMapping(path = "/{id}", consumes = { "application/json" },
                produces = { "application/json" })
    @Operation(summary = "Edit group with ID", tags = { "groups" },
               description = "Either updates existing or saves new "
                       + "group, returns result")
    @ApiResponse(responseCode = "200", description = "operation successful",
                 content = @Content(
                         schema = @Schema(implementation = Group.class)))
    @ApiResponse(responseCode = "400", description = "invalid data submitted",
                 content = @Content(
                         schema = @Schema(implementation = ApiException.class)))
    EntityModel<Group> editById(@PathVariable @Min(1) long id,
            @RequestBody @Valid Group newGroup);

    @DeleteMapping(path = "/{id}", produces = { "application/json" })
    @Operation(summary = "Delete group by ID", tags = { "groups" })
    @ApiResponse(responseCode = "400", description = "invalid ID submitted",
                 content = @Content(
                         schema = @Schema(implementation = ApiException.class)))
    @ApiResponse(responseCode = "501", description = "not yet implemented",
                 content = @Content(
                         schema = @Schema(implementation = ApiException.class)))
    ResponseEntity<Void> deleteById(@PathVariable @Min(1) long id);

}
