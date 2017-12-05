package com.ocds.access.controller;

import com.ocds.access.exception.ValidationException;
import com.ocds.access.model.dto.ein.EinDto;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ein")
public class EinController {

    @PostMapping(value = "/save")
    @ResponseStatus(HttpStatus.CREATED)
    public EinDto saveEnquiry(@Valid @RequestBody final EinDto dataDto,
                              final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }
        return dataDto;

    }
}