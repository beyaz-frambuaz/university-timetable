package com.shablii.timetable.api.rest;

import com.shablii.timetable.config.ControllersTestConfig;
import com.shablii.timetable.service.utility.SemesterCalendar;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SemesterController.class)
@Import(ControllersTestConfig.class)
class SemesterControllerTest {

    @Autowired
    private MockMvc mvc;

    @SpyBean
    private SemesterCalendar semesterCalendar;

    @Test
    void getSemesterShouldRequestInstanceFromCalendar() throws Exception {

        SemesterCalendar.Semester semester = semesterCalendar.getSemester();

        mvc.perform(get("/api/v1/timetable/semester/").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startDate").value(semester.getStartDate().toString()))
                .andExpect(jsonPath("$.endDate").value(semester.getEndDate().toString()))
                .andExpect(jsonPath("$.lengthInWeeks").value(semester.getLengthInWeeks()))
                .andExpect(jsonPath("$._links.self").isNotEmpty());

        then(semesterCalendar).should(times(2)).getSemester();
    }

}