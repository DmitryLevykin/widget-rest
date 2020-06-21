package com.levykin.widget.dao.impl;

import com.levykin.widget.dao.WidgetDao;
import com.levykin.widget.exception.WidgetNotFoundException;
import com.levykin.widget.model.Widget;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Profile("java-in-memory-storage")
@Component
public class JavaInMemoryWidgetDao implements WidgetDao {

  private final Map<Long, Widget> map = new ConcurrentHashMap<>();
  private final AtomicLong idSequenceCounter = new AtomicLong(0L);

  private static Sort SORT_BY_INDEX = Sort.by("index");
  private static Sort SORT_BY_INDEX_DESC = Sort.by("index").descending();

  @Override
  public long add(Widget creationWidget) {
    Widget widget = new Widget(creationWidget);
    widget.setId(idSequenceCounter.incrementAndGet());
    map.put(widget.getId(), widget);
    return widget.getId();
  }

  @Override
  public void delete(long id) {
    checkExisting(id);
    map.remove(id);
  }

  @Override
  public void update(Widget updateWidget) {
    Long id = updateWidget.getId();
    checkExisting(id);
    map.put(id, new Widget(updateWidget));
  }

  @Override
  public Widget get(long id) {
    Widget widget = map.get(id);
    if (widget == null) {
      throw new WidgetNotFoundException(id);
    }
    return new Widget(widget);
  }

  @Override
  public Iterable<Widget> getAll(Sort sort) {
    return getSortedStream(sort).map(Widget::new).collect(Collectors.toList());
  }

  @Override
  public Page<Widget> getAll(Pageable pageable) {
    int from = pageable.getPageNumber() * pageable.getPageSize();
    int limit = pageable.getPageSize();
    List<Widget> widgets =
        getSortedStream(pageable.getSort())
            .skip(from)
            .limit(limit)
            .map(Widget::new)
            .collect(Collectors.toList());
    return new PageImpl<>(widgets, pageable, map.size());
  }

  private Stream<Widget> getSortedStream(Sort sort) {
    if (sort.equals(SORT_BY_INDEX)) {
      return map.values().stream().sorted(Comparator.comparingInt(Widget::getIndex));
    }
    if (sort.equals(SORT_BY_INDEX_DESC)) {
      return map.values().stream().sorted(Comparator.comparingInt(Widget::getIndex).reversed());
    }
    throw new IllegalStateException(
        JavaInMemoryWidgetDao.class.getName() + " supports sorting only by index");
  }

  private void checkExisting(long id) {
    if (!map.containsKey(id)) {
      throw new WidgetNotFoundException(id);
    }
  }
}
