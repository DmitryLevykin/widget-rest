package com.levykin.widget.exception;

abstract class ServiceException extends RuntimeException {

  ServiceException(String message) {
    super(message);
  }
}
