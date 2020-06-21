package com.levykin.widget.dao;

import com.levykin.widget.exception.WidgetNotFoundException;
import com.levykin.widget.model.Widget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

abstract class AbstractDaoTest {

  private WidgetDao dao;

  protected abstract WidgetDao getDao();

  @BeforeEach
  private void initDao() {
    dao = getDao();
  }

  private Widget getTestWidget() {
    Widget widget = new Widget();
    widget.setX(1);
    widget.setY(2);
    widget.setIndex(3);
    widget.setWidth(4.0f);
    widget.setHeight(5.0f);
    widget.setModificationDate(new Date());
    return widget;
  }

  @Test
  void widgetShouldBeTheSameAfterCreation() {
    Widget creationWidget = getTestWidget();
    long id = dao.add(creationWidget);
    Widget createdWidget = dao.get(id);
    assertNotNull(createdWidget.getId());
    assertEquals(creationWidget.getX(), createdWidget.getX());
    assertEquals(creationWidget.getY(), createdWidget.getY());
    assertEquals(creationWidget.getWidth(), createdWidget.getWidth());
    assertEquals(creationWidget.getHeight(), createdWidget.getHeight());
    assertEquals(creationWidget.getModificationDate(), createdWidget.getModificationDate());
  }

  @Test
  void widgetShouldMatchTheChangesAfterUpdate() {
    long id = dao.add(new Widget());
    Widget updateWidget = getTestWidget();
    updateWidget.setId(id);
    dao.update(updateWidget);
    Widget updatedWidget = dao.get(id);
    assertNotNull(updatedWidget.getId());
    assertEquals(updateWidget.getX(), updatedWidget.getX());
    assertEquals(updateWidget.getY(), updatedWidget.getY());
    assertEquals(updateWidget.getWidth(), updatedWidget.getWidth());
    assertEquals(updateWidget.getHeight(), updatedWidget.getHeight());
    assertEquals(updateWidget.getModificationDate(), updatedWidget.getModificationDate());
  }

  @Test
  void widgetShouldBeAbsentAfterDelete() {
    long id = dao.add(new Widget());
    dao.delete(id);
    assertThrows(WidgetNotFoundException.class, () -> dao.get(id));
  }

  @Test
  void shouldThrowExceptionOnUpdateAbsent() {
    Widget updateWidget = getTestWidget();
    updateWidget.setId(0L);
    assertThrows(WidgetNotFoundException.class, () -> dao.update(updateWidget));
  }

  @Test
  void shouldThrowExceptionOnDeleteAbsent() {
    assertThrows(WidgetNotFoundException.class, () -> dao.delete(0L));
  }

  @Test
  void widgetsShouldBePresentOnGetAllPageable() {
    Set<Long> createdIds = new HashSet<>();
    createdIds.add(dao.add(getTestWidget()));
    createdIds.add(dao.add(getTestWidget()));
    createdIds.add(dao.add(getTestWidget()));
    Iterable<Widget> widgets = dao.getAll(PageRequest.of(0, 5, Sort.by("index")));
    Set<Long> gotIds =
        StreamSupport.stream(widgets.spliterator(), false)
            .map(Widget::getId)
            .collect(Collectors.toSet());
    assertEquals(createdIds, gotIds);
  }
}
