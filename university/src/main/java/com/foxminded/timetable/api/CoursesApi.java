package com.foxminded.timetable.api;

import com.foxminded.timetable.exceptions.ApiException;
import com.foxminded.timetable.model.*;
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

@RequestMapping("/api/v1/timetable/courses")
@Validated
@Tag(name = "courses", description = "Courses API")
public interface CoursesApi {

    @GetMapping(produces = { "application/json" })
    @Operation(summary = "List all courses", tags = { "courses" })
    @ApiResponse(responseCode = "200", description = "operation successful",
                 content = @Content(array = @ArraySchema(
                         schema = @Schema(implementation = Course.class))))
    CollectionModel<EntityModel<Course>> findAll();

    @PostMapping(consumes = { "application/json" },
                 produces = { "application/json" })
    @Operation(summary = "Create new course", tags = { "courses" })
    @ApiResponse(responseCode = "201", description = "course created",
                 content = @Content(
                         schema = @Schema(implementation = Course.class)))
    @ApiResponse(responseCode = "400", description = "invalid data submitted",
                 content = @Content(
                         schema = @Schema(implementation = ApiException.class)))
    ResponseEntity<EntityModel<Course>> addNew(
            @RequestBody @Valid Course course);

    @GetMapping(path = "/{id}", produces = { "application/json" })
    @Operation(summary = "Find course by ID", tags = { "courses" })
    @ApiResponse(responseCode = "200", description = "found",
                 content = @Content(
                         schema = @Schema(implementation = Course.class)))
    @ApiResponse(responseCode = "400", description = "invalid ID submitted",
                 content = @Content(
                         schema = @Schema(implementation = ApiException.class)))
    @ApiResponse(responseCode = "404", description = "course not found",
                 content = @Content(
                         schema = @Schema(implementation = ApiException.class)))
    EntityModel<Course> findById(@PathVariable @Min(1) long id);

    @PutMapping(path = "/{id}", consumes = { "application/json" },
                produces = { "application/json" })
    @Operation(summary = "Edit course with ID", tags = { "courses" },
               description = "Either updates existing or saves new "
                       + "course, returns result")
    @ApiResponse(responseCode = "200", description = "operation successful",
                 content = @Content(
                         schema = @Schema(implementation = Course.class)))
    @ApiResponse(responseCode = "400", description = "invalid data submitted",
                 content = @Content(
                         schema = @Schema(implementation = ApiException.class)))
    EntityModel<Course> editById(@PathVariable @Min(1) long id,
            @RequestBody @Valid Course newCourse);

    @DeleteMapping(path = "/{id}", produces = { "application/json" })
    @Operation(summary = "Delete course by ID", tags = { "courses" })
    @ApiResponse(responseCode = "400", description = "invalid ID submitted",
                 content = @Content(
                         schema = @Schema(implementation = ApiException.class)))
    @ApiResponse(responseCode = "501", description = "not yet implemented",
                 content = @Content(
                         schema = @Schema(implementation = ApiException.class)))
    ResponseEntity<Void> deleteById(@PathVariable @Min(1) long id);

    @GetMapping(path = "/{id}/professors", produces = { "application/json" })
    @Operation(summary = "Find professors teaching course",
               tags = { "courses", "professors" })
    @ApiResponse(responseCode = "200", description = "found",
                 content = @Content(array = @ArraySchema(
                         schema = @Schema(implementation = Professor.class))))
    @ApiResponse(responseCode = "400", description = "invalid ID submitted",
                 content = @Content(
                         schema = @Schema(implementation = ApiException.class)))
    @ApiResponse(responseCode = "404", description = "course not found",
                 content = @Content(
                         schema = @Schema(implementation = ApiException.class)))
    CollectionModel<EntityModel<Professor>> findCourseProfessors(
            @PathVariable @Min(1) long id);

}
