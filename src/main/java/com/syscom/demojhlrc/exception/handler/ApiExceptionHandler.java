package com.syscom.demojhlrc.exception.handler;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

	private final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(value = { IOException.class })
	protected ResponseEntity<Object> handleException(RuntimeException runtimeException, WebRequest request) {
		logError(runtimeException);
		return handleExceptionInternal(runtimeException, "Elastic search exception.", new HttpHeaders(),
				HttpStatus.INTERNAL_SERVER_ERROR, request);
	}

	private void logError(Exception exception) {
		logger.error(exception.getMessage(), exception);
	}

}
