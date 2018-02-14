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
    public ResponseEntity<ResponseDto> getLots(@RequestParam final String cpId,
                                               @RequestParam final String status) {
        return new ResponseEntity<>(lotsService.getLots(cpId, TenderStatus.fromValue(status)), HttpStatus.OK);
    }

    @PostMapping("/updateStatus")
    public ResponseEntity<ResponseDto> updateStatus(@RequestParam final String cpId,
                                                    @RequestParam final String status,
                                                    @Valid @RequestBody final LotsRequestDto lotsDto) {
        return new ResponseEntity<>(
                lotsService.updateStatus(cpId, TenderStatus.fromValue(status), lotsDto),
                HttpStatus.OK);
    }

    @PostMapping("/updateStatusDetails")
    public ResponseEntity<ResponseDto> updateStatusDetails(@RequestParam final String cpId,
                                                           @RequestParam final String statusDetails,
                                                           @Valid @RequestBody final LotsRequestDto lotsDto) {
        return new ResponseEntity<>(
                lotsService.updateStatusDetails(cpId, TenderStatusDetails.fromValue(statusDetails), lotsDto),
                HttpStatus.OK);
    }
}
