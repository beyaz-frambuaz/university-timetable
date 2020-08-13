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

@RequestMapping("/api/v1/timetable/professors")
@Validated
@Tag(name = "professors", description = "Professors API")
public interface ProfessorsApi {

    @GetMapping(produces = { "application/json" })
    @Operation(summary = "List all professors", tags = { "professors" })
    @ApiResponse(responseCode = "200", description = "operation successful",
                 content = @Content(array = @ArraySchema(
                         schema = @Schema(implementation = Professor.class))))
    CollectionModel<EntityModel<Professor>> findAll();

    @PostMapping(consumes = { "application/json" },
                 produces = { "application/json" })
    @Operation(summary = "Create new professor", tags = { "professors" })
    @ApiResponse(responseCode = "201", description = "professor created",
                 content = @Content(
                         schema = @Schema(implementation = Professor.class)))
    @ApiResponse(responseCode = "400", description = "invalid data submitted",
                 content = @Content(
                         schema = @Schema(implementation = ApiException.class)))
    ResponseEntity<EntityModel<Professor>> addNew(
            @RequestBody @Valid Professor professor);

    @GetMapping(path = "/{id}", produces = { "application/json" })
    @Operation(summary = "Find professor by ID", tags = { "professors" })
    @ApiResponse(responseCode = "200", description = "found",
                 content = @Content(
                         schema = @Schema(implementation = Professor.class)))
    @ApiResponse(responseCode = "400", description = "invalid ID submitted",
                 content = @Content(
                         schema = @Schema(implementation = ApiException.class)))
    @ApiResponse(responseCode = "404", description = "professor not found",
                 content = @Content(
                         schema = @Schema(implementation = ApiException.class)))
    EntityModel<Professor> findById(@PathVariable @Min(1) long id);

    @PutMapping(path = "/{id}", consumes = { "application/json" },
                produces = { "application/json" })
    @Operation(summary = "Edit professor with ID", tags = { "professors" },
               description = "Either updates existing or saves new "
                       + "professor, returns result")
    @ApiResponse(responseCode = "200", description = "operation successful",
                 content = @Content(
                         schema = @Schema(implementation = Professor.class)))
    @ApiResponse(responseCode = "400", description = "invalid data submitted",
                 content = @Content(
                         schema = @Schema(implementation = ApiException.class)))
    EntityModel<Professor> editById(@PathVariable @Min(1) long id,
            @RequestBody @Valid Professor newProfessor);

    @DeleteMapping(path = "/{id}", produces = { "application/json" })
    @Operation(summary = "Delete professor by ID", tags = { "professors" })
    @ApiResponse(responseCode = "400", description = "invalid ID submitted",
                 content = @Content(
                         schema = @Schema(implementation = ApiException.class)))
    @ApiResponse(responseCode = "501", description = "not yet implemented",
                 content = @Content(
                         schema = @Schema(implementation = ApiException.class)))
    ResponseEntity<Void> deleteById(@PathVariable @Min(1) long id);

    @GetMapping(path = "/{id}/courses", produces = { "application/json" })
    @Operation(summary = "Find professor's courses",
               tags = { "professors", "courses" })
    @ApiResponse(responseCode = "200", description = "found",
                 content = @Content(array = @ArraySchema(
                         schema = @Schema(implementation = Course.class))))
    @ApiResponse(responseCode = "400", description = "invalid ID submitted",
                 content = @Content(
                         schema = @Schema(implementation = ApiException.class)))
    @ApiResponse(responseCode = "404", description = "professor not found",
                 content = @Content(
                         schema = @Schema(implementation = ApiException.class)))
    CollectionModel<EntityModel<Course>> findProfessorCourses(
            @PathVariable @Min(1) long id);

    @GetMapping(path = "/{id}/courses/{courseId}/students",
                produces = { "application/json" })
    @Operation(summary = "Find students attending professor's course",
               tags = { "professors", "courses", "students" })
    @ApiResponse(responseCode = "200", description = "found",
                 content = @Content(array = @ArraySchema(
                         schema = @Schema(implementation = Student.class))))
    @ApiResponse(responseCode = "400",
                 description = "invalid professor or course ID submitted",
                 content = @Content(
                         schema = @Schema(implementation = ApiException.class)))
    @ApiResponse(responseCode = "404",
                 description = "professor or course not found",
                 content = @Content(
                         schema = @Schema(implementation = ApiException.class)))
    CollectionModel<EntityModel<Student>> findCourseAttendees(
            @PathVariable @Min(1) long id, @PathVariable @Min(1) long courseId);

}
