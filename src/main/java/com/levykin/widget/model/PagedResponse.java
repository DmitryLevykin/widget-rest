package com.levykin.widget.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;

import java.util.List;

public class PagedResponse<T> {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Long total;

  private boolean hasNext;

  private List<T> content;

  private int page;

  private int size;

  public PagedResponse(Slice<T> slice) {
    hasNext = slice.hasNext();
    content = slice.getContent();
    size = slice.getSize();
    page = slice.getNumber();
    if (slice instanceof Page) {
      total = ((Page) slice).getTotalElements();
    }
  }

  public Long getTotal() {
    return total;
  }

  public boolean isHasNext() {
    return hasNext;
  }

  public List<T> getContent() {
    return content;
  }

  public int getPage() {
    return page;
  }

  public int getSize() {
    return size;
  }
}
