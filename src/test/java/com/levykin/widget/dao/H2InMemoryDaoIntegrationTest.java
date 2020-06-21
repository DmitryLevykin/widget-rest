package com.levykin.widget.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("h2-in-memory-storage")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class H2InMemoryDaoIntegrationTest extends AbstractDaoTest {

  @Autowired private WidgetDao dao;

  @Override
  protected WidgetDao getDao() {
    return dao;
  }
}
