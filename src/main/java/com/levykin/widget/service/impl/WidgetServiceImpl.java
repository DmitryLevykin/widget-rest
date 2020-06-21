package com.levykin.widget.service.impl;

import com.levykin.widget.dao.WidgetDao;
import com.levykin.widget.model.AreaFilter;
import com.levykin.widget.model.Widget;
import com.levykin.widget.service.WidgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Service
public class WidgetServiceImpl implements WidgetService {

  @Autowired private WidgetDao dao;

  @Value("${widget-application.max-page-size:500}")
  private int maxPageSize;

  @Value("${widget-application.default-page-size:100}")
  private int defaultPageSize;

  @Override
  public Widget get(long id) {
    return dao.get(id);
  }

  @Override
  public void delete(long id) {
    dao.delete(id);
  }

  @Override
  public Slice<Widget> getAll(Pageable pageable, AreaFilter area) {
    if (pageable == null) {
      pageable = getDefaultPageable();
    } else {
      int pageSize = Math.min(maxPageSize, pageable.getPageSize());
      pageable = PageRequest.of(pageable.getPageNumber(), pageSize, SORT_BY_INDEX);
    }
    if (area == null) {
      return dao.getAll(pageable);
    }
    return getAllFiltered(pageable, area);
  }

  @Override
  public synchronized long create(Widget widget) {
    widget.setId(null);
    widget.setModificationDate(new Date());
    takeIndexAndShiftNext(widget);
    return dao.add(widget);
  }

  @Override
  public synchronized Widget update(Widget widget) {
    widget.setModificationDate(new Date());
    takeIndexAndShiftNext(widget);
    dao.update(widget);
    return widget;
  }

  private Pageable getDefaultPageable() {
    return PageRequest.of(0, defaultPageSize, SORT_BY_INDEX);
  }

  private Slice<Widget> getAllFiltered(Pageable pageable, AreaFilter area) {
    // We need to know if there are exist any more matched widgets
    int limit = pageable.getPageSize() + 1;

    List<Widget> widgets = getFilteredPart(pageable, area, limit);

    boolean hasNext = widgets.size() > pageable.getPageSize();
    List<Widget> content = hasNext ? widgets.subList(0, pageable.getPageSize()) : widgets;

    return new SliceImpl<>(content, pageable, hasNext);
  }

  private List<Widget> getFilteredPart(Pageable pageable, AreaFilter area, int limit) {
    int from = pageable.getPageNumber() * pageable.getPageSize();
    Iterable<Widget> widgetIterable = dao.getAll(pageable.getSort());
    Iterator<Widget> iterator = widgetIterable.iterator();
    int offsetCounter = 0;
    List<Widget> widgets = new ArrayList<>(limit);
    while (iterator.hasNext() && widgets.size() < limit) {
      Widget widget = iterator.next();
      if (!area.match(widget)) {
        continue;
      }
      if (offsetCounter++ < from) {
        continue;
      }
      widgets.add(widget);
    }
    return widgets;
  }

  private int getMaxIndex() {
    Iterator<Widget> iterator = dao.getAll(SORT_BY_INDEX_DESC).iterator();
    if (iterator.hasNext()) {
      return iterator.next().getIndex();
    }
    return 0;
  }

  private void takeIndexAndShiftNext(Widget activeWidget) {
    int maxIndex = getMaxIndex();
    if (activeWidget.getIndex() == null || activeWidget.getIndex() > maxIndex) {
      activeWidget.setIndex(maxIndex + 1);
      return; // Nothing to shift. Use latest index.
    }

    Integer existingIndex = getIndexById(activeWidget.getId());
    if (activeWidget.getIndex().equals(existingIndex)) {
      return;
    }

    List<Widget> widgetsToShift =
        getNextWidgetsToShift(activeWidget.getIndex(), activeWidget.getId());
    // Shifting widgets in reverse order
    for (int i = widgetsToShift.size() - 1; i >= 0; i--) {
      Widget widget = widgetsToShift.get(i);
      widget.setIndex(widget.getIndex() + 1);
      dao.update(widget);
    }
  }

  private List<Widget> getNextWidgetsToShift(int fromIndex, Long activeWidgetId) {
    int prevWidgetIndex = fromIndex;
    List<Widget> widgetsToShift = new ArrayList<>();
    Iterable<Widget> widgets = dao.getAll(SORT_BY_INDEX);
    for (Widget widget : widgets) {
      Integer widgetIndex = widget.getIndex();
      if (fromIndex > widgetIndex) {
        continue;
      }
      if (widgetIndex - prevWidgetIndex > 1) {
        // Reach a gap
        break;
      }
      if (widget.getId().equals(activeWidgetId)) {
        // Reach the position of source widget. It means a gap too.
        break;
      }
      prevWidgetIndex = widgetIndex;
      widgetsToShift.add(widget);
    }
    return widgetsToShift;
  }

  private Integer getIndexById(Long widgetId) {
    return widgetId != null ? dao.get(widgetId).getIndex() : null;
  }
}
