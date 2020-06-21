package com.levykin.widget.exception;

public class WidgetNotFoundException extends ServiceException {

  public WidgetNotFoundException(long id) {
    super("Widget not found: " + id);
  }
}
