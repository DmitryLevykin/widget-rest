package com.levykin.widget.dao.impl;

import com.levykin.widget.model.Widget;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
interface WidgetRepository extends PagingAndSortingRepository<Widget, Long> {}
