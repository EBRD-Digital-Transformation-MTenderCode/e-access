package com.procurement.access.controller;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.ein.EinDto;
import com.procurement.access.model.dto.ein.UpdateFsDto;
import com.procurement.access.service.EinService;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/ein")
public class EinController {

    private final EinService einService;

    public EinController(final EinService einService) {
        this.einService = einService;
    }

    @PostMapping(value = "/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseDto create(@Valid @RequestBody final EinDto einDto) {
        return einService.createEin(einDto);
    }

    @PostMapping(value = "/update")
    @ResponseStatus(HttpStatus.OK)
    public ResponseDto update(@Valid @RequestBody final EinDto einDto) {
        return einService.updateEin(einDto);
    }

    @PostMapping(value = "/addRelatedProcess")
    @ResponseStatus(HttpStatus.OK)
    public ResponseDto addRelatedProcess(@Valid @RequestBody final UpdateFsDto updateFsDto) {
        return einService.addRelatedProcess(updateFsDto);
    }
}
