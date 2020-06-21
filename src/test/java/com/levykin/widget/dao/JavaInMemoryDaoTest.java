package com.levykin.widget.dao;

import com.levykin.widget.dao.impl.JavaInMemoryWidgetDao;

class JavaInMemoryDaoTest extends AbstractDaoTest {

  @Override
  protected WidgetDao getDao() {
    return new JavaInMemoryWidgetDao();
  }
}
