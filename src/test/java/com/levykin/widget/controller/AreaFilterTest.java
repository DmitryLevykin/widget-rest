package com.levykin.widget.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.levykin.widget.model.AreaFilter;
import com.levykin.widget.model.Widget;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class AreaFilterTest {

  private static final String WIDGET_JSON_FILE = "area_test_widgets.json";

  private static List<Widget> widgets;

  @BeforeAll
  static void initWidgets() throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    ClassPathResource classPathResource = new ClassPathResource(WIDGET_JSON_FILE);
    try (InputStream inputStream = classPathResource.getInputStream()) {
      CollectionType type =
          objectMapper.getTypeFactory().constructCollectionType(List.class, Widget.class);
      widgets = objectMapper.readValue(inputStream, type);
    }
  }

  @Test
  void checkWidgetsInArea1() {
    Set<Long> matchedIds = getMatchedWidgets(new AreaFilter(0, 0, 1, 1));
    Assertions.assertTrue(matchedIds.contains(1L));
    Assertions.assertFalse(Stream.of(2L, 3L, 4L, 5L, 6L, 7L, 8L).anyMatch(matchedIds::contains));
  }

  @Test
  void checkWidgetsInArea2() {
    Set<Long> matchedIds = getMatchedWidgets(new AreaFilter(0, 0, 3, 3));
    Assertions.assertTrue(matchedIds.containsAll(Arrays.asList(1L, 2L, 3L, 5L, 6L, 7L, 8L)));
    Assertions.assertFalse(matchedIds.contains(4L));
  }

  private Set<Long> getMatchedWidgets(AreaFilter areaFilter) {
    return widgets.stream()
        .filter(areaFilter::match)
        .map(Widget::getId)
        .collect(Collectors.toSet());
  }
}
