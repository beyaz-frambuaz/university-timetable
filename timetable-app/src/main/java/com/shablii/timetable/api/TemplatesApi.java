package com.shablii.timetable.api;

import com.shablii.timetable.exceptions.ApiException;
import com.shablii.timetable.model.ScheduleTemplate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;

@RequestMapping("/api/v1/timetable/templates")
@Validated
@Tag(name = "templates", description = "Templates API")
public interface TemplatesApi {

    @GetMapping(produces = { "application/json" })
    @Operation(summary = "List all templates", tags = { "templates" },
               description = "Lists schedule templates that represent " + "two week study cycle")
    @ApiResponse(responseCode = "200", description = "operation successful",
                 content = @Content(array = @ArraySchema(schema = @Schema(implementation = ScheduleTemplate.class))))
    CollectionModel<EntityModel<ScheduleTemplate>> findAll();

    @GetMapping(path = "/{id}", produces = { "application/json" })
    @Operation(summary = "Find template by ID", tags = { "templates" },
               description = "Get single schedule template that is used to " + "generate by-weekly schedule items")
    @ApiResponse(responseCode = "200", description = "found",
                 content = @Content(schema = @Schema(implementation = ScheduleTemplate.class)))
    @ApiResponse(responseCode = "400", description = "invalid ID submitted",
                 content = @Content(schema = @Schema(implementation = ApiException.class)))
    @ApiResponse(responseCode = "404", description = "template not found",
                 content = @Content(schema = @Schema(implementation = ApiException.class)))
    EntityModel<ScheduleTemplate> findById(@PathVariable @Min(1) long id);

}
