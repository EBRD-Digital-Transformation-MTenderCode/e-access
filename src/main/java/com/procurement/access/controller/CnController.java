package com.procurement.access.controller;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.tender.TenderDto;
import com.procurement.access.service.CnService;
import java.time.LocalDateTime;
import javax.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
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
    public ResponseEntity<ResponseDto> create(@RequestParam final String owner,
                                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                              @RequestParam(required = false) final LocalDateTime startDate,
                                              @Valid @RequestBody final TenderDto tenderDto) {
        return new ResponseEntity<>(
                cnService.createCn(owner, startDate, tenderDto),
                HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<ResponseDto> update(@RequestParam final String cpId,
                                              @RequestParam final String token,
                                              @RequestParam final String owner,
                                              @Valid @RequestBody final TenderDto tenderDto) {
        return new ResponseEntity<>(cnService.updateCn(owner, cpId, token, tenderDto), HttpStatus.OK);
    }
}
