package com.levykin.widget.service;

import com.levykin.widget.model.AreaFilter;
import com.levykin.widget.model.Widget;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

public interface WidgetService {

  Sort SORT_BY_INDEX = Sort.by("index");

  Sort SORT_BY_INDEX_DESC = Sort.by("index").descending();

  Widget get(long id);

  void delete(long id);

  long create(Widget widget);

  Widget update(Widget widget);

  Slice<Widget> getAll(Pageable pageable, AreaFilter area);
}
