package com.procurement.access.controller;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.ein.EinDto;
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

    @PostMapping
    public ResponseEntity<ResponseDto> create(@RequestParam("owner") final String owner,
                                              @Valid @RequestBody final EinDto einDto) {
        return new ResponseEntity<>(einService.createEin(owner, einDto), HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<ResponseDto> update(@RequestParam("owner") final String owner,
                                              @RequestParam("identifier") final String identifier,
                                              @RequestParam("token") final String token,
                                              @Valid @RequestBody final EinDto einDto) {
        return new ResponseEntity<>(einService.updateEin(owner, identifier, token, einDto), HttpStatus.OK);
    }
}
