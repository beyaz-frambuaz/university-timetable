package com.shablii.timetable.mvc;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IndexController.class)
class IndexControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void indexShouldHandleGetIndex() throws Exception {

        mvc.perform(get("/index")).andExpect(status().isOk()).andExpect(view().name("index"));
    }

    @Test
    public void indexShouldHandleGetDefault() throws Exception {

        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("index"));
    }

    @Test
    public void indexShouldRemoveStudentAndProfessorFromSession() throws Exception {

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(request().sessionAttributeDoesNotExist("student", "professor"));
    }

}