package com.levykin.widget.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.levykin.widget.dao.WidgetDao;
import com.levykin.widget.dao.impl.JavaInMemoryWidgetDao;
import com.levykin.widget.model.PagedResponse;
import com.levykin.widget.model.Widget;
import com.levykin.widget.service.WidgetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Profile("java-in-memory-storage")
class WidgetControllerIntegrationTest {

  private static final String WIDGET_JSON_FILE = "controller_initial_test_widgets.json";

  @Autowired private WidgetController controller;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private WidgetService service;

  private WidgetDao dao;

  @BeforeEach
  private void resetDao() {
    dao = new JavaInMemoryWidgetDao();
    ReflectionTestUtils.setField(service, "dao", dao);
  }

  void initTestWidgetsFromJsonFile() throws IOException {
    ClassPathResource classPathResource = new ClassPathResource(WIDGET_JSON_FILE);
    try (InputStream inputStream = classPathResource.getInputStream()) {
      TypeFactory typeFactory = objectMapper.getTypeFactory();
      CollectionType type = typeFactory.constructCollectionType(List.class, Widget.class);
      List<Widget> widgets = objectMapper.readValue(inputStream, type);
      widgets.forEach(widget -> controller.create(widget));
    }
  }

  @Test
  void shouldContainsNoWidgetsOnEmptyStorage() {
    PagedResponse<Widget> response = controller.list(null, null, null, null, null, null);
    assertTrue(response.getContent().isEmpty());
  }

  @Test
  void shouldContainsWidgetsOnDefaultPage() {
    initWidgetsWithSpecifiedIndexes(0, 1, 5);
    PagedResponse<Widget> response = controller.list(null, null, null, null, null, null);
    assertFalse(response.getContent().isEmpty());
  }

  @Test
  void shouldReturnWidget() throws IOException {
    initTestWidgetsFromJsonFile();
    Widget widget = controller.get(1L);
    assertEquals(0, widget.getIndex());
    assertEquals(0, widget.getX());
    assertEquals(0, widget.getY());
    assertEquals(3, widget.getWidth());
    assertEquals(4, widget.getHeight());
  }

  @Test
  void shouldContainsWidgetsOnPageOne() throws IOException {
    initTestWidgetsFromJsonFile();
    PagedResponse<Widget> response = controller.list(1, 5, null, null, null, null);
    assertFalse(response.getContent().isEmpty());
  }

  //  0  1  2  3  4  5  6  7  8  9
  // [1][2][3][4][5][6][7][8][9][10]
  // [1][2][_][4][_][_][7][8][9][_]
  @Test
  void shouldNotContainsDeleted() {
    initWidgetsWithSpecifiedIndexes(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    Arrays.asList(3L, 5L, 6L, 10L).forEach(id -> controller.delete(id));
    PagedResponse<Widget> response = controller.list(0, 10, null, null, null, null);
    Set<Long> ids = response.getContent().stream().map(Widget::getId).collect(Collectors.toSet());
    assertFalse(ids.contains(3L));
    assertFalse(ids.contains(5L));
    assertFalse(ids.contains(6L));
    assertFalse(ids.contains(10L));
  }

  //  1  2  3  4
  // [1][2][_][3]
  // [2][1][_][3]
  @Test
  void shouldSwapWithNextGapOnUpdate() {
    initWidgetsWithSpecifiedIndexes(1, 2, 4);
    Widget widget = new Widget(controller.get(2L));
    widget.setIndex(1);
    controller.update(widget, widget.getId());
    assertEquals(1, controller.get(2L).getIndex());
    assertEquals(2, controller.get(1L).getIndex());
  }

  //  1  2  3  4  5  6  7  8  9
  // [2][1][_][3][_][_][4][5][6]
  // [2][1][_][3][_][_][5][4][6]
  @Test
  void shouldSwapWithoutNextGapOnUpdate() {
    initWidgetsWithSpecifiedIndexes(2, 1, 4, 7, 8, 9);
    Widget widget = new Widget(controller.get(5L));
    widget.setIndex(7);
    controller.update(widget, widget.getId());
    assertEquals(7, controller.get(5L).getIndex());
    assertEquals(8, controller.get(4L).getIndex());
    assertEquals(9, controller.get(6L).getIndex());
  }

  //  1  2  3  4  5  6  7  8  9  10 11 12
  // [2][1][_][3][_][_][5][6][4][_][8][7]
  // [2][1][_][3][_][_][9][5][6][4][8][7]
  @Test
  void shouldShiftToNextGapOnCreation() {
    initWidgetsWithSpecifiedIndexes(2, 1, 4, 9, 7, 8, 12, 11);
    Widget widget = new Widget();
    widget.setIndex(7);
    controller.create(widget);
    assertEquals(7, controller.get(9L).getIndex());
    assertEquals(8, controller.get(5L).getIndex());
    assertEquals(9, controller.get(6L).getIndex());
    assertEquals(10, controller.get(4L).getIndex());
    assertEquals(11, controller.get(8L).getIndex());
  }

  private void initWidgetsWithSpecifiedIndexes(Integer... indexes) {
    for (Integer index : indexes) {
      Widget widget = new Widget();
      widget.setIndex(index);
      dao.add(widget);
    }
  }
}
