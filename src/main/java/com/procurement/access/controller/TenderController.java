package com.procurement.access.controller;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.ocds.TenderStatus;
import com.procurement.access.model.dto.ocds.TenderStatusDetails;
import com.procurement.access.service.TenderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/tender")
public class TenderController {

    private final TenderService tenderService;

    public TenderController(final TenderService tenderService) {
        this.tenderService = tenderService;
    }

    @PostMapping("/updateStatus")
    public ResponseEntity<ResponseDto> updateStatus(@RequestParam("identifier") final String cpId,
                                                    @RequestParam("stage") final String stage,
                                                    @RequestParam("status") final String status) {
        return new ResponseEntity<>(
                tenderService.updateStatus(cpId, stage, TenderStatus.fromValue(status)),
                HttpStatus.OK);
    }

    @PostMapping("/updateStatusDetails")
    public ResponseEntity<ResponseDto> updateStatusDetails(@RequestParam("identifier") final String cpId,
                                                           @RequestParam("stage") final String stage,
                                                           @RequestParam("statusDetails") final String statusDetails) {
        return new ResponseEntity<>(
                tenderService.updateStatusDetails(cpId, stage, TenderStatusDetails.fromValue(statusDetails)),
                HttpStatus.OK);
    }

    @PostMapping("/setSuspended")
    public ResponseEntity<ResponseDto> setSuspended(@RequestParam("identifier") final String cpId,
                                                    @RequestParam("stage") final String stage,
                                                    @RequestParam("suspended") final Boolean suspended) {
        return new ResponseEntity<>(
                tenderService.setSuspended(cpId, stage, suspended),
                HttpStatus.OK);
    }
}
