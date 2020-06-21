package com.levykin.widget.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.levykin.widget.exception.WidgetNotFoundException;
import com.levykin.widget.model.Widget;
import com.levykin.widget.service.WidgetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WidgetControllerTest {

  private static final int NUMBER_OF_TEST_WIDGETS = 10;

  @Autowired private MockMvc mockMvc;

  @MockBean private WidgetService widgetService;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void shouldReturnWidgetsOnEmptyGetRequest() throws Exception {
    List<Widget> widgets = new ArrayList<>();
    for (int i = 0; i < NUMBER_OF_TEST_WIDGETS; i++) {
      Widget widget = new Widget();
      widget.setId((long) i);
      widgets.add(widget);
    }
    PageRequest pageable = PageRequest.of(0, NUMBER_OF_TEST_WIDGETS);
    PageImpl<Widget> page = new PageImpl<>(widgets, pageable, NUMBER_OF_TEST_WIDGETS);
    when(widgetService.getAll(isNull(), isNull())).thenReturn(page);
    mockMvc
        .perform(get("/widget"))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.hasNext", is(false)))
        .andExpect(jsonPath("$.total", is(NUMBER_OF_TEST_WIDGETS)))
        .andExpect(jsonPath("$.page", is(0)))
        .andExpect(jsonPath("$.size", is(NUMBER_OF_TEST_WIDGETS)))
        .andExpect(jsonPath("$.content", hasSize(NUMBER_OF_TEST_WIDGETS)));
  }

  @Test
  void shouldReturnExistingWidgetOnGet() throws Exception {
    Widget widget = new Widget();
    widget.setId(1L);
    widget.setX(100);
    when(widgetService.get(1L)).thenReturn(widget);
    mockMvc
        .perform(get("/widget/1"))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.x", is(100)))
        .andExpect(status().isOk());
  }

  @Test
  void shouldReturnNotFoundStatusWhenOnNotFoundException() throws Exception {
    when(widgetService.get(anyLong())).thenThrow(new WidgetNotFoundException(1L));
    mockMvc
        .perform(get("/widget/1"))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());

    doThrow(new WidgetNotFoundException(2L)).when(widgetService).delete(anyLong());
    mockMvc
        .perform(delete("/widget/1"))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldReturnInternalErrorOnOtherExceptions() throws Exception {
    when(widgetService.get(1L)).thenThrow(new RuntimeException("Some test exception"));
    mockMvc
        .perform(get("/widget/1"))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());

    when(widgetService.get(2L)).thenThrow(new Error("Some test error"));
    mockMvc
        .perform(get("/widget/2"))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void shouldReturnBadRequest() throws Exception {
    mockMvc
        .perform(get("/widget/k"))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    mockMvc
        .perform(get("/widget?page=k"))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturnBadRequestOnValidationErrorsOnCreation() throws Exception {
    Widget widget = new Widget();
    mockMvc
        .perform(
            post("/widget")
                .content(objectMapper.writeValueAsString(widget))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    widget.setX(1);
    mockMvc
        .perform(
            post("/widget")
                .content(objectMapper.writeValueAsString(widget))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    widget.setY(2);
    mockMvc
        .perform(
            post("/widget")
                .content(objectMapper.writeValueAsString(widget))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    widget.setWidth(10f);
    mockMvc
        .perform(
            post("/widget")
                .content(objectMapper.writeValueAsString(widget))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    widget.setHeight(20f);
    mockMvc
        .perform(
            post("/widget")
                .content(objectMapper.writeValueAsString(widget))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  void shouldReturnBadRequestOnValidationErrorsOnUpdate() throws Exception {
    Widget widget = new Widget();
    widget.setX(1);
    widget.setY(2);
    widget.setWidth(10f);
    widget.setHeight(20f);
    mockMvc
        .perform(
            put("/widget/1")
                .content(objectMapper.writeValueAsString(widget))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    widget.setIndex(1);
    mockMvc
        .perform(
            put("/widget/1")
                .content(objectMapper.writeValueAsString(widget))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }
}
