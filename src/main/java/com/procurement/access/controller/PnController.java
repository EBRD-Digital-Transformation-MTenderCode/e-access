package com.procurement.access.controller;

import com.procurement.access.model.dto.pn.PnDto;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pn")
public class PnController {

    @PostMapping(value = "/save")
    @ResponseStatus(HttpStatus.CREATED)
    public PnDto saveEnquiry(@Valid @RequestBody final PnDto dataDto) {
        return dataDto;
    }
}