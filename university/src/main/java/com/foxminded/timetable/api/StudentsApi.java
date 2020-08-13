package com.foxminded.timetable.api;

import com.foxminded.timetable.exceptions.ApiException;
import com.foxminded.timetable.model.Student;
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

@RequestMapping("/api/v1/timetable/students")
@Validated
@Tag(name = "students", description = "Students API")
public interface StudentsApi {

    @GetMapping(produces = { "application/json" })
    @Operation(summary = "List all students", tags = { "students" })
    @ApiResponse(responseCode = "200", description = "operation successful",
                 content = @Content(array = @ArraySchema(
                         schema = @Schema(implementation = Student.class))))
    CollectionModel<EntityModel<Student>> findAll();

    @PostMapping(consumes = { "application/json" },
                 produces = { "application/json" })
    @Operation(summary = "Create new student", tags = { "students" })
    @ApiResponse(responseCode = "201", description = "student created",
                 content = @Content(
                         schema = @Schema(implementation = Student.class)))
    @ApiResponse(responseCode = "400", description = "invalid data submitted",
                 content = @Content(
                         schema = @Schema(implementation = ApiException.class)))
    ResponseEntity<EntityModel<Student>> addNew(
            @RequestBody @Valid Student student);

    @GetMapping(path = "/{id}", produces = { "application/json" })
    @Operation(summary = "Find student by ID", tags = { "students" })
    @ApiResponse(responseCode = "200", description = "found",
                 content = @Content(
                         schema = @Schema(implementation = Student.class)))
    @ApiResponse(responseCode = "400", description = "invalid ID submitted",
                 content = @Content(
                         schema = @Schema(implementation = ApiException.class)))
    @ApiResponse(responseCode = "404", description = "student not found",
                 content = @Content(
                         schema = @Schema(implementation = ApiException.class)))
    EntityModel<Student> findById(@PathVariable @Min(1) long id);

    @PutMapping(path = "/{id}", consumes = { "application/json" },
                produces = { "application/json" })
    @Operation(summary = "Edit student with ID", tags = { "students" },
               description = "Either updates existing or saves new "
                       + "student, returns result")
    @ApiResponse(responseCode = "200", description = "operation successful",
                 content = @Content(
                         schema = @Schema(implementation = Student.class)))
    @ApiResponse(responseCode = "400", description = "invalid data submitted",
                 content = @Content(
                         schema = @Schema(implementation = ApiException.class)))
    EntityModel<Student> editById(@PathVariable @Min(1) long id,
            @RequestBody @Valid Student newStudent);

    @DeleteMapping(path = "/{id}", produces = { "application/json" })
    @Operation(summary = "Delete student by ID", tags = { "students" })
    @ApiResponse(responseCode = "400", description = "invalid ID submitted",
                 content = @Content(
                         schema = @Schema(implementation = ApiException.class)))
    @ApiResponse(responseCode = "501", description = "not yet implemented",
                 content = @Content(
                         schema = @Schema(implementation = ApiException.class)))
    ResponseEntity<Void> deleteById(@PathVariable @Min(1) long id);

}
