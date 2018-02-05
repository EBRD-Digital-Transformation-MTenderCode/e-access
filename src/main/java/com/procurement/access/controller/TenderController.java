package com.procurement.access.controller;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.ocds.TenderStatus;
import com.procurement.access.model.dto.ocds.TenderStatusDetails;
import com.procurement.access.service.TenderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/tender")
public class TenderController {

    private final TenderService tenderService;

    public TenderController(final TenderService tenderService) {
        this.tenderService = tenderService;
    }

    @PatchMapping("/updateStatus")
    public ResponseEntity<ResponseDto> updateStatus(final String cpId,
                                                    final String status) {
        return new ResponseEntity<>(
                tenderService.updateStatus(cpId, TenderStatus.fromValue(status)),
                HttpStatus.OK);
    }

    @PatchMapping("/updateStatusDetails")
    public ResponseEntity<ResponseDto> updateStatusDetails(final String cpId,
                                                           final String statusDetails) {
        return new ResponseEntity<>(
                tenderService.updateStatusDetails(cpId, TenderStatusDetails.fromValue(statusDetails)),
                HttpStatus.OK);
    }
}
