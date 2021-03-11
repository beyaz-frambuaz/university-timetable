package com.shablii.timetable.api;

import com.shablii.timetable.service.utility.SemesterCalendar;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/timetable/semester")
@Tag(name = "semester", description = "Semester API")
public interface SemesterApi {

    @GetMapping(produces = { "application/json" })
    @Operation(summary = "Get semester description", tags = { "semester" },
               description = "Returns representation of current semester with"
                       + " start and end dates and length in weeks. Data can be" + " used for schedule lookups")
    @ApiResponse(responseCode = "200", description = "operation successful",
                 content = @Content(schema = @Schema(implementation = SemesterCalendar.Semester.class)))
    EntityModel<SemesterCalendar.Semester> getSemester();

}
