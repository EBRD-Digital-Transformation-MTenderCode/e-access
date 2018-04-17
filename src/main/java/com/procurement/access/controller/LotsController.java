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
    public ResponseEntity<ResponseDto> getLots(@RequestParam("identifier") final String cpId,
                                               @RequestParam("stage") final String stage,
                                               @RequestParam("status") final String status) {
        return new ResponseEntity<>(
                lotsService.getLots(cpId, stage, TenderStatus.fromValue(status)),
                HttpStatus.OK);
    }

    @PostMapping("/updateStatus")
    public ResponseEntity<ResponseDto> updateStatus(@RequestParam("identifier") final String cpId,
                                                    @RequestParam("stage") final String stage,
                                                    @RequestParam("status") final String status,
                                                    @Valid @RequestBody final LotsRequestDto data) {
        return new ResponseEntity<>(
                lotsService.updateStatus(cpId, stage, TenderStatus.fromValue(status), data),
                HttpStatus.OK);
    }

    @PostMapping("/updateStatusDetails")
    public ResponseEntity<ResponseDto> updateStatusDetails(@RequestParam("identifier") final String cpId,
                                                           @RequestParam("stage") final String stage,
                                                           @RequestParam("statusDetails") final String statusDetails,
                                                           @Valid @RequestBody final LotsRequestDto data) {
        return new ResponseEntity<>(
                lotsService.updateStatusDetails(cpId, stage, TenderStatusDetails.fromValue(statusDetails), data),
                HttpStatus.OK);
    }
}
