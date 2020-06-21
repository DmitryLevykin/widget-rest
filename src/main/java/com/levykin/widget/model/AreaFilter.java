package com.levykin.widget.model;

public class AreaFilter {

  private int x;

  private int y;

  private int width;

  private int height;

  public AreaFilter(int x, int y, int width, int height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }

  public boolean match(Widget widget) {
    return widget.getX() >= x
        && widget.getY() >= y
        && widget.getX() + widget.getWidth() <= x + width
        && widget.getY() + widget.getHeight() <= y + height;
  }
}
