package com.procurement.access.controller;

import com.procurement.access.exception.ValidationException;
import com.procurement.access.model.dto.bpe.ResponseDto;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ControllerExceptionHandler {
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ValidationException.class)
    public ResponseDto handleValidationContractProcessPeriod(
        final ValidationException e) {
        return new ResponseDto(false, getErrors(e.getErrors()), null);
    }

    private List<ResponseDto.ResponseDetailsDto> getErrors(final BindingResult errors) {
        return errors.getFieldErrors()
                     .stream()
                     .map(f -> new ResponseDto.ResponseDetailsDto(f.getCode(),
                                                                  f.getField() + " : " + f.getDefaultMessage()))
                     .collect(Collectors.toList());
    }
}
