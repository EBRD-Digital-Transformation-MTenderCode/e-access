package com.procurement.access.controller;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.ein.EinDto;
import com.procurement.access.model.dto.ein.UpdateFsDto;
import com.procurement.access.service.EinService;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/ein")
public class EinController {

    private final EinService einService;

    public EinController(final EinService einService) {
        this.einService = einService;
    }

    @PostMapping(value = "/create")
    public ResponseEntity<ResponseDto> create(@RequestParam("country") final String country,
                                              @RequestParam("pmd") final String pmd,
                                              @RequestParam("stage") final String stage,
                                              @RequestParam("owner") final String owner,
                                              @Valid @RequestBody final EinDto einDto) {
        return new ResponseEntity<>(einService.createEin(country, pmd, stage, owner, einDto), HttpStatus.CREATED);
    }

    @PostMapping(value = "/update")
    public ResponseEntity<ResponseDto> update(@RequestParam("country") final String country,
                                              @RequestParam("pmd") final String pmd,
                                              @RequestParam("stage") final String stage,
                                              @RequestParam("owner") final String owner,
                                              @Valid @RequestBody final EinDto einDto) {
        return new ResponseEntity<>(einService.updateEin(einDto), HttpStatus.OK);
    }

    @PostMapping(value = "/updateAmountByFs")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ResponseDto> updateByFs(@Valid @RequestBody final UpdateFsDto updateFsDto) {
        return new ResponseEntity<>(einService.updateAmountByFs(updateFsDto), HttpStatus.OK);
    }
}
