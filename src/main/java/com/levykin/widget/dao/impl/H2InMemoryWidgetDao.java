package com.levykin.widget.dao.impl;

import com.levykin.widget.dao.WidgetDao;
import com.levykin.widget.exception.WidgetNotFoundException;
import com.levykin.widget.model.Widget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Profile("h2-in-memory-storage")
@Scope("singleton")
@Component
public class H2InMemoryWidgetDao implements WidgetDao {

  @Autowired private WidgetRepository repository;

  @Override
  public long add(Widget widget) {
    repository.save(widget);
    return widget.getId();
  }

  @Override
  public void update(Widget widget) {
    checkExisting(widget.getId());
    repository.save(widget);
  }

  @Override
  public void delete(long id) {
    checkExisting(id);
    repository.deleteById(id);
  }

  @Override
  public Widget get(long id) {
    return repository.findById(id).orElseThrow(() -> new WidgetNotFoundException(id));
  }

  @Override
  public Iterable<Widget> getAll(Sort sort) {
    return repository.findAll(sort);
  }

  @Override
  public Page<Widget> getAll(Pageable pageable) {
    return repository.findAll(pageable);
  }

  private void checkExisting(long id) {
    if (!repository.existsById(id)) {
      throw new WidgetNotFoundException(id);
    }
  }
}
