package com.levykin.widget.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.Date;

@Entity
public class Widget {

  public interface Creation {}

  public interface Update {}

  @Id
  @Null(groups = {Creation.class})
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @NotNull(groups = {Creation.class, Update.class})
  private Integer x;

  @NotNull(groups = {Creation.class, Update.class})
  private Integer y;

  @NotNull(groups = {Update.class})
  private Integer index;

  @NotNull(groups = {Creation.class, Update.class})
  private Float width;

  @NotNull(groups = {Creation.class, Update.class})
  private Float height;

  @Null(groups = {Creation.class, Update.class})
  private Date modificationDate;

  public Widget() {}

  public Widget(Widget widget) {
    this.id = widget.id;
    this.x = widget.x;
    this.y = widget.y;
    this.width = widget.width;
    this.height = widget.height;
    this.index = widget.index;
    this.modificationDate = widget.modificationDate;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Integer getX() {
    return x;
  }

  public void setX(Integer x) {
    this.x = x;
  }

  public Integer getY() {
    return y;
  }

  public void setY(Integer y) {
    this.y = y;
  }

  public Integer getIndex() {
    return index;
  }

  public void setIndex(Integer index) {
    this.index = index;
  }

  public Float getWidth() {
    return width;
  }

  public void setWidth(Float width) {
    this.width = width;
  }

  public Float getHeight() {
    return height;
  }

  public void setHeight(Float height) {
    this.height = height;
  }

  public Date getModificationDate() {
    return modificationDate;
  }

  public void setModificationDate(Date modificationDate) {
    this.modificationDate = modificationDate;
  }
}
