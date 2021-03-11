package com.shablii.timetable.api;

import com.shablii.timetable.exceptions.ApiException;
import com.shablii.timetable.model.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.time.LocalDate;

@RequestMapping("/api/v1/timetable/schedules")
@Validated
@Tag(name = "schedules", description = "Schedules API")
public interface SchedulesApi {

    @GetMapping(path = "/date", produces = { "application/json" })
    @Operation(summary = "Date schedules", tags = { "schedules" },
               description = "Look up schedule items by semester date "
                       + "(optionally filter by either professor, group, " + "course, or auditorium")
    @ApiResponse(responseCode = "200", description = "operation successful",
                 content = @Content(array = @ArraySchema(schema = @Schema(implementation = Schedule.class))))
    @ApiResponse(responseCode = "400", description = "invalid data submitted",
                 content = @Content(schema = @Schema(implementation = ApiException.class)))
    CollectionModel<EntityModel<Schedule>> findByDate(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(name = "professorId", required = false) @Min(1) Long professorId,
            @RequestParam(name = "groupId", required = false) @Min(1) Long groupId,
            @RequestParam(name = "courseId", required = false) @Min(1) Long courseId,
            @RequestParam(name = "auditoriumId", required = false) @Min(1) Long auditoriumId);

    @GetMapping(path = "/week", produces = { "application/json" })
    @Operation(summary = "Week schedules", tags = { "schedules" },
               description = "Look up schedule items by semester week number "
                       + "(optionally filter by either professor, group, " + "course, or auditorium")
    @ApiResponse(responseCode = "200", description = "operation successful",
                 content = @Content(array = @ArraySchema(schema = @Schema(implementation = Schedule.class))))
    @ApiResponse(responseCode = "400", description = "invalid data submitted",
                 content = @Content(schema = @Schema(implementation = ApiException.class)))
    CollectionModel<EntityModel<Schedule>> findByWeek(@RequestParam("week") @Min(1) int week,
            @RequestParam(name = "professorId", required = false) @Min(1) Long professorId,
            @RequestParam(name = "groupId", required = false) @Min(1) Long groupId,
            @RequestParam(name = "courseId", required = false) @Min(1) Long courseId,
            @RequestParam(name = "auditoriumId", required = false) @Min(1) Long auditoriumId);

    @GetMapping(path = "/month", produces = { "application/json" })
    @Operation(summary = "Month schedules", tags = { "schedules" },
               description = "Look up schedule items by month " + "(optionally filter by either professor, group, "
                       + "course, or auditorium")
    @ApiResponse(responseCode = "200", description = "operation successful",
                 content = @Content(array = @ArraySchema(schema = @Schema(implementation = Schedule.class))))
    @ApiResponse(responseCode = "400", description = "invalid data submitted",
                 content = @Content(schema = @Schema(implementation = ApiException.class)))
    CollectionModel<EntityModel<Schedule>> findByMonth(@RequestParam("month") @Min(1) int month,
            @RequestParam(name = "professorId", required = false) @Min(1) Long professorId,
            @RequestParam(name = "groupId", required = false) @Min(1) Long groupId,
            @RequestParam(name = "courseId", required = false) @Min(1) Long courseId,
            @RequestParam(name = "auditoriumId", required = false) @Min(1) Long auditoriumId);

    @GetMapping(path = "/{id}", produces = { "application/json" })
    @Operation(summary = "Find schedule item by ID", tags = { "schedules" })
    @ApiResponse(responseCode = "200", description = "found",
                 content = @Content(schema = @Schema(implementation = Schedule.class)))
    @ApiResponse(responseCode = "400", description = "invalid ID submitted",
                 content = @Content(schema = @Schema(implementation = ApiException.class)))
    @ApiResponse(responseCode = "404", description = "schedule not found",
                 content = @Content(schema = @Schema(implementation = ApiException.class)))
    EntityModel<Schedule> findById(@PathVariable @Min(1) long id);

    @PutMapping(path = "/{id}", consumes = { "application/json" }, produces = { "application/json" })
    @Operation(summary = "Reschedule", tags = { "schedules" }, description = "Use to make one-time changes (substitute "
            + "professor, move to a different auditorium, etc.) or" + " reschedule all class occurrences. "
            + "Saves edited schedule item. " + "Optionally reschedules recurring class which "
            + "involves editing existing schedules and underlying " + "template (all further schedule items will be "
            + "generated per edited template)")
    @ApiResponse(responseCode = "200", description = "operation successful",
                 content = @Content(schema = @Schema(implementation = Schedule.class)))
    @ApiResponse(responseCode = "400",
                 description = "May be produced for multiple reasons:\n" + "1. Invalid ID\n 2. Invalid query data\n"
                         + "3. Path ID and schedule ID don't match\n"
                         + "4. Schedule illegally changed group or course\n"
                         + "5. Submitted schedule is inconsistent with "
                         + "existing timetable and might create collisions",
                 content = @Content(schema = @Schema(implementation = ApiException.class)))
    @ApiResponse(responseCode = "404", description = "schedule not found",
                 content = @Content(schema = @Schema(implementation = ApiException.class)))
    CollectionModel<EntityModel<Schedule>> reschedule(@PathVariable @Min(1) long id,
            @RequestBody @Valid Schedule rescheduled,
            @RequestParam(name = "recurring", defaultValue = "false") boolean recurring);

    @GetMapping(path = "/{id}/available/auditoriums", produces = { "application/json" })
    @Operation(summary = "Find available auditoriums", tags = { "schedules", "auditoriums" },
               description = "Return result can be used to move class to a new "
                       + "auditorium without timetable collisions. Empty list"
                       + " is returned when no auditoriums are available.")
    @ApiResponse(responseCode = "200", description = "operation successful",
                 content = @Content(array = @ArraySchema(schema = @Schema(implementation = Auditorium.class))))
    @ApiResponse(responseCode = "400", description = "invalid ID submitted",
                 content = @Content(schema = @Schema(implementation = ApiException.class)))
    @ApiResponse(responseCode = "404", description = "schedule not found",
                 content = @Content(schema = @Schema(implementation = ApiException.class)))
    CollectionModel<EntityModel<Auditorium>> findAvailableAuditoriums(@PathVariable @Min(1) long id);

    @GetMapping(path = "/{id}/available/professors", produces = { "application/json" })
    @Operation(summary = "Find available professors", tags = { "schedules", "professors" },
               description = "Return result can be used to substitute "
                       + "professor without timetable collisions. Empty list"
                       + " is returned when no professors are available.")
    @ApiResponse(responseCode = "200", description = "operation successful",
                 content = @Content(array = @ArraySchema(schema = @Schema(implementation = Professor.class))))
    @ApiResponse(responseCode = "400", description = "invalid ID submitted",
                 content = @Content(schema = @Schema(implementation = ApiException.class)))
    @ApiResponse(responseCode = "404", description = "schedule not found",
                 content = @Content(schema = @Schema(implementation = ApiException.class)))
    CollectionModel<EntityModel<Professor>> findAvailableProfessors(@PathVariable @Min(1) long id);

    @GetMapping(path = "/{id}/options", produces = { "application/json" })
    @Operation(summary = "Find rescheduling options", tags = { "schedules" },
               description = "Return result can be used to edit schedule "
                       + "items without timetable collisions. Empty list"
                       + " is returned when no options are available.")
    @ApiResponse(responseCode = "200", description = "operation successful",
                 content = @Content(array = @ArraySchema(schema = @Schema(implementation = ReschedulingOption.class))))
    @ApiResponse(responseCode = "400", description = "invalid data submitted",
                 content = @Content(schema = @Schema(implementation = ApiException.class)))
    @ApiResponse(responseCode = "404", description = "schedule not found",
                 content = @Content(schema = @Schema(implementation = ApiException.class)))
    CollectionModel<EntityModel<ReschedulingOption>> findOptions(@PathVariable @Min(1) long id,
            @RequestParam(name = "date", required = false) @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(name = "week", required = false) @Min(1) Integer week);

}
