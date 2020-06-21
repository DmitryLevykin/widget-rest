package com.levykin.widget.exception;

import com.levykin.widget.model.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolationException;

@ControllerAdvice
class ExceptionAdvice {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @ResponseBody
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  private ErrorResponse handleException(Exception ex) {
    logger.error(ex.getMessage(), ex);
    return new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
  }

  @ResponseBody
  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(WidgetNotFoundException.class)
  private ErrorResponse handleNotFoundException(WidgetNotFoundException ex) {
    logger.info(ex.getMessage(), ex);
    return new ErrorResponse(ex.getMessage());
  }

  @ResponseBody
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler({
    ServletRequestBindingException.class,
    MethodArgumentTypeMismatchException.class,
    ConstraintViolationException.class,
    MethodArgumentNotValidException.class
  })
  private ErrorResponse handleBadRequestException(Exception ex) {
    logger.info(ex.getMessage(), ex);
    return new ErrorResponse(ex.getMessage());
  }
}
