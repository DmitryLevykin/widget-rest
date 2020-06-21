package com.levykin.widget.controller;

import com.levykin.widget.model.AreaFilter;
import com.levykin.widget.model.IdentifierResponse;
import com.levykin.widget.model.PagedResponse;
import com.levykin.widget.model.Widget;
import com.levykin.widget.service.WidgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Min;

@RequestMapping("/widget")
@RestController
@Validated
class WidgetController {

  @Autowired private WidgetService service;

  @GetMapping("/{id}")
  Widget get(@PathVariable Long id) {
    return service.get(id);
  }

  @DeleteMapping(value = "/{id}")
  void delete(@PathVariable Long id) {
    service.delete(id);
  }

  @PostMapping
  IdentifierResponse create(@Validated(Widget.Creation.class) @RequestBody Widget widget) {
    return new IdentifierResponse(service.create(widget));
  }

  @PutMapping(value = "/{id}")
  Widget update(@Validated(Widget.Update.class) @RequestBody Widget widget, @PathVariable Long id) {
    widget.setId(id);
    return service.update(widget);
  }

  @GetMapping
  PagedResponse<Widget> list(
      @Min(0) @RequestParam(value = "page", required = false) Integer page,
      @Min(1) @RequestParam(value = "size", required = false) Integer size,
      @RequestParam(value = "area_x", required = false) Integer x,
      @RequestParam(value = "area_y", required = false) Integer y,
      @Min(1) @RequestParam(value = "area_width", required = false) Integer width,
      @Min(1) @RequestParam(value = "area_height", required = false) Integer height) {
    Pageable pageable = page != null && size != null ? PageRequest.of(page, size) : null;
    AreaFilter areaFilter = null;
    if (x != null && y != null && width != null && height != null) {
      areaFilter = new AreaFilter(x, y, width, height);
    }
    return new PagedResponse<>(service.getAll(pageable, areaFilter));
  }
}
