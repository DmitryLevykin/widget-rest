package com.levykin.widget.dao;

import com.levykin.widget.model.Widget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface WidgetDao {

  long add(Widget data);

  void update(Widget data);

  void delete(long id);

  Widget get(long id);

  Iterable<Widget> getAll(Sort sort);

  Page<Widget> getAll(Pageable pageable);
}
