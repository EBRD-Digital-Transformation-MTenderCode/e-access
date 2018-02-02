package com.procurement.access.controller;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.lots.LotsRequestDto;
import com.procurement.access.model.dto.ocds.TenderStatus;
import com.procurement.access.model.dto.ocds.TenderStatusDetails;
import com.procurement.access.service.LotsService;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/lots")
public class LotsController {

    private final LotsService lotsService;

    public LotsController(final LotsService lotsService) {
        this.lotsService = lotsService;
    }

    @GetMapping
    public ResponseEntity<ResponseDto> getLots(final String cpId,
                                               final String status) {
        return new ResponseEntity<>(lotsService.getLots(cpId, TenderStatus.fromValue(status)), HttpStatus.OK);
    }

    @PutMapping("/updateStatus")
    public ResponseEntity<ResponseDto> updateStatus(final String cpId,
                                                    final String status,
                                                    @Valid @RequestBody final LotsRequestDto lotsDto) {
        return new ResponseEntity<>(
                lotsService.updateStatus(cpId, TenderStatus.fromValue(status), lotsDto),
                HttpStatus.OK);
    }

    @PutMapping("/updateStatusDetails")
    public ResponseEntity<ResponseDto> updateStatusDetails(final String cpId,
                                                           final String statusDetails,
                                                           @Valid @RequestBody final LotsRequestDto lotsDto) {
        return new ResponseEntity<>(
                lotsService.updateStatusDetails(cpId, TenderStatusDetails.fromValue(statusDetails), lotsDto),
                HttpStatus.OK);
    }
}
