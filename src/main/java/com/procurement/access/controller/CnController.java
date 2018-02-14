package com.procurement.access.controller;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.tender.TenderDto;
import com.procurement.access.service.CnService;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/cn")
public class CnController {

    private final CnService cnService;

    public CnController(final CnService cnService) {
        this.cnService = cnService;
    }

    @PostMapping
    public ResponseEntity<ResponseDto> create(final String owner,
                                              @Valid @RequestBody final TenderDto tenderDto) {
        return new ResponseEntity<>(cnService.createCn(owner, tenderDto), HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<ResponseDto> update(final String cpId,
                                              final String token,
                                              final String owner,
                                              @Valid @RequestBody final TenderDto tenderDto) {
        return new ResponseEntity<>(cnService.updateCn(owner, cpId, token, tenderDto), HttpStatus.OK);
    }
}
