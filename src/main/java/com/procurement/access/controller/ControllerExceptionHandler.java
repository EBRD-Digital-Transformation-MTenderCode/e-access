package com.procurement.access.controller;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.procurement.access.exception.ErrorException;
import com.procurement.access.exception.ValidationException;
import com.procurement.access.model.dto.bpe.ResponseDetailsDto;
import com.procurement.access.model.dto.bpe.ResponseDto;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.validation.ConstraintViolationException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.OK;

@ControllerAdvice
public class ControllerExceptionHandler {

    @ResponseBody
    @ResponseStatus(OK)
    @ExceptionHandler(NullPointerException.class)
    public ResponseDto handleNullPointerException(final NullPointerException e) {
        return new ResponseDto<>(false, getErrors(e.getClass().getName(), e.getMessage()), null);
    }

    @ResponseBody
    @ResponseStatus(OK)
    @ExceptionHandler(ValidationException.class)
    public ResponseDto handleValidationContractProcessPeriod(final ValidationException e) {
        return new ResponseDto<>(false, getErrors(e.getErrors()), null);
    }

    @ResponseBody
    @ResponseStatus(OK)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseDto methodArgumentNotValidException(final MethodArgumentNotValidException e) {
        return new ResponseDto<>(false, getErrors(e.getBindingResult()), null);
    }

    @ResponseBody
    @ResponseStatus(OK)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseDto handle(final ConstraintViolationException e) {
        return new ResponseDto<>(false, getErrors(e), null);
    }

    @ResponseBody
    @ResponseStatus(OK)
    @ExceptionHandler(JsonMappingException.class)
    public ResponseDto handleJsonMappingExceptionException(final JsonMappingException e) {
        return new ResponseDto<>(false, getErrors(e.getClass().getName(), e.getMessage()), null);
    }

    @ResponseBody
    @ResponseStatus(OK)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseDto handleJsonMappingExceptionException(final IllegalArgumentException e) {
        return new ResponseDto<>(false, getErrors(e.getClass().getName(), e.getMessage()), null);
    }

    @ResponseBody
    @ResponseStatus(OK)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseDto handleJsonMappingExceptionException(final MethodArgumentTypeMismatchException e) {
        return new ResponseDto<>(false, getErrors(e.getClass().getName(), e.getMessage()), null);
    }

    @ResponseBody
    @ResponseStatus(OK)
    @ExceptionHandler(ErrorException.class)
    public ResponseDto handleErrorInsertException(final ErrorException e) {
        return new ResponseDto<>(false, getErrors(e.getClass().getName(), e.getMessage()), null);
    }

    @ResponseBody
    @ResponseStatus(OK)
    @ExceptionHandler(ServletException.class)
    public ResponseDto handleErrorInsertException(final ServletException e) {
        return new ResponseDto<>(false, getErrors(e.getClass().getName(), e.getMessage()), null);
    }

    private List<ResponseDetailsDto> getErrors(final BindingResult result) {
        return result.getFieldErrors()
                .stream()
                .map(f -> new ResponseDetailsDto(
                        f.getField(),
                        f.getCode() + " : " + f.getDefaultMessage()))
                .collect(Collectors.toList());
    }

    private List<ResponseDetailsDto> getErrors(final ConstraintViolationException e) {
        return e.getConstraintViolations()
                .stream()
                .map(f -> new ResponseDetailsDto(
                        f.getPropertyPath().toString(),
                        f.getMessage() + " " + f.getMessageTemplate()))
                .collect(toList());
    }

    private List<ResponseDetailsDto> getErrors(final String code, final String error) {
        return Arrays.asList(new ResponseDetailsDto(code, error));
    }
}
