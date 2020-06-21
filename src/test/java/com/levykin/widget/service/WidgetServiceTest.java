package com.levykin.widget.service;

import com.levykin.widget.dao.WidgetDao;
import com.levykin.widget.model.AreaFilter;
import com.levykin.widget.model.Widget;
import com.levykin.widget.service.impl.WidgetServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.levykin.widget.service.WidgetService.SORT_BY_INDEX;
import static com.levykin.widget.service.WidgetService.SORT_BY_INDEX_DESC;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class WidgetServiceTest {

  private static final int TEST_PAGE_SIZE = 10;

  @Mock private WidgetDao dao;

  private WidgetService service;

  @BeforeEach
  private void initService() {
    service = new WidgetServiceImpl();
    ReflectionTestUtils.setField(service, "dao", dao);
    ReflectionTestUtils.setField(service, "maxPageSize", TEST_PAGE_SIZE);
    ReflectionTestUtils.setField(service, "defaultPageSize", TEST_PAGE_SIZE);
  }

  @Test
  void shouldRequestPaginationFromZeroWhenItAbsent() {
    service.getAll(null, null);
    ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    verify(dao).getAll(pageableCaptor.capture());
    Pageable pageable = pageableCaptor.getValue();
    assertEquals(SORT_BY_INDEX, pageable.getSort());
    assertEquals(0, pageable.getPageNumber());
  }

  @Test
  void shouldFillModificationDateAndIndexAfterCreation() {
    service.create(new Widget());
    ArgumentCaptor<Widget> widgetCaptor = ArgumentCaptor.forClass(Widget.class);
    verify(dao).add(widgetCaptor.capture());
    Widget widget = widgetCaptor.getValue();
    assertNotNull(widget.getModificationDate());
    assertNotNull(widget.getIndex());
  }

  @Test
  void shouldUpdateModificationDateAfterUpdate() {
    Widget widget = createWidget(0L, 0);
    when(dao.get(0L)).thenReturn(widget);
    service.update(widget);
    ArgumentCaptor<Widget> widgetCaptor = ArgumentCaptor.forClass(Widget.class);
    verify(dao).update(widgetCaptor.capture());
    assertNotNull(widgetCaptor.getValue().getModificationDate());
  }

  @Test
  void shouldReturnOnlyMatchedWhenUsesArea() {
    mock10Widgets();
    AreaFilter area = mock(AreaFilter.class);
    // Match only with even indexes
    Answer<Boolean> answer = invocation -> ((Widget) invocation.getArgument(0)).getId() % 2 == 0;
    when(area.match(any(Widget.class))).thenAnswer(answer);
    assertEquals(asList(2L, 4L), getAllWidgetIdOnPage(0, area));
    assertEquals(asList(6L, 8L), getAllWidgetIdOnPage(1, area));
    assertEquals(Collections.singletonList(10L), getAllWidgetIdOnPage(2, area));
  }

  @Test
  void shouldShiftNextWidgetsAfterCreationWithSameIndex() {
    mock10Widgets();
    Widget widget = createWidget(0);
    service.create(widget);
    verify(dao).add(widget);
    ArgumentCaptor<Widget> widgetCaptor = ArgumentCaptor.forClass(Widget.class);
    verify(dao, times(10)).update(widgetCaptor.capture());
    List<Widget> updatedWidgets = widgetCaptor.getAllValues();
    updatedWidgets.sort(Comparator.comparingInt(Widget::getIndex));
    for (int i = 0; i < updatedWidgets.size(); i++) {
      assertEquals(i + 1, updatedWidgets.get(i).getIndex());
    }
  }

  @Test
  void shouldNotUpdateNextWidgetsAfterCreationWithLatestIndex() {
    mock10Widgets();
    Widget widget = createWidget(10);
    service.create(widget);
    verify(dao).add(widget);
    verify(dao, never()).update(any(Widget.class));
  }

  @Test
  void shouldTakeLatestIndexAfterCreationWithoutIndex() {
    service.create(new Widget());
    mockDaoWidgets(Collections.singletonList(createWidget(1L, 0)));
    service.create(createWidget(0));
    mockDaoWidgets(Arrays.asList(createWidget(2L, 0), createWidget(1L, 1)));
    service.create(new Widget());
    ArgumentCaptor<Widget> widgetCaptor = ArgumentCaptor.forClass(Widget.class);
    verify(dao, times(3)).add(widgetCaptor.capture());
    assertEquals(2, widgetCaptor.getValue().getIndex());
  }

  @Test
  void shouldShiftWidgetsWithBelowZeroIndexes() {
    service.create(createWidget(-1));
    service.create(createWidget(1));
    mockDaoWidgets(Arrays.asList(createWidget(1, -1), createWidget(2, 0)));
    service.create(createWidget(-1));
    ArgumentCaptor<Widget> widgetCaptor = ArgumentCaptor.forClass(Widget.class);
    verify(dao, times(2)).update(widgetCaptor.capture());
    List<Widget> updatedWidgets = widgetCaptor.getAllValues();
    updatedWidgets.sort(Comparator.comparingInt(Widget::getIndex));
    assertEquals(0, updatedWidgets.get(0).getIndex());
    assertEquals(1, updatedWidgets.get(1).getIndex());
  }

  @Test
  void shouldSwapIndexesAfterUpdate() {
    service.create(new Widget());
    mockDaoWidget(createWidget(1, 0));
    service.create(new Widget());
    List<Widget> widgets = Arrays.asList(createWidget(1, 0), createWidget(2, 1));
    mockDaoWidgets(widgets);
    when(dao.get(2)).thenReturn(widgets.get(1));
    service.update(createWidget(2, 0));
    ArgumentCaptor<Widget> widgetCaptor = ArgumentCaptor.forClass(Widget.class);
    verify(dao, times(2)).update(widgetCaptor.capture());
    List<Widget> updatedWidgets = widgetCaptor.getAllValues();
    assertEquals(1, updatedWidgets.get(0).getId());
    assertEquals(1, updatedWidgets.get(0).getIndex());
    assertEquals(2, updatedWidgets.get(1).getId());
    assertEquals(0, updatedWidgets.get(1).getIndex());
  }

  @Test
  void shouldNotSwapIndexOnUpdateWithSameIndex() {
    service.create(new Widget());
    mockDaoWidget(createWidget(1, 0));
    service.create(new Widget());
    List<Widget> widgets = Arrays.asList(createWidget(1, 0), createWidget(2, 1));
    mockDaoWidgets(widgets);
    service.update(widgets.get(1));
    ArgumentCaptor<Widget> widgetCaptor = ArgumentCaptor.forClass(Widget.class);
    verify(dao).update(widgetCaptor.capture());
    assertEquals(2, widgetCaptor.getValue().getId());
    assertEquals(1, widgetCaptor.getValue().getIndex());
  }

  private List<Long> getAllWidgetIdOnPage(int page, AreaFilter area) {
    Pageable pageable = PageRequest.of(page, 2);
    return service.getAll(pageable, area).getContent().stream()
        .map(Widget::getId)
        .collect(Collectors.toList());
  }

  private Widget createWidget(int index) {
    Widget widget = new Widget();
    widget.setIndex(index);
    return widget;
  }

  private Widget createWidget(long id, int index) {
    Widget widget = createWidget(index);
    widget.setId(id);
    return widget;
  }

  private void mockDaoWidget(Widget widget) {
    mockDaoWidgets(Collections.singletonList(widget));
  }

  private void mockDaoWidgets(List<Widget> widgets) {
    ArrayList<Widget> reversedWidgets = new ArrayList<>(widgets);
    Collections.reverse(reversedWidgets);
    when(dao.getAll(SORT_BY_INDEX)).thenReturn(widgets);
    when(dao.getAll(SORT_BY_INDEX_DESC)).thenReturn(reversedWidgets);
    when(dao.getAll(any(Pageable.class))).thenReturn(new PageImpl<>(widgets));
    widgets.forEach(widget -> when(dao.get(widget.getId())).thenReturn(widget));
  }

  private void mock10Widgets() {
    List<Widget> widgets =
        IntStream.range(0, 10)
            .mapToObj(index -> createWidget(index + 1, index))
            .collect(Collectors.toList());
    mockDaoWidgets(widgets);
  }
}
