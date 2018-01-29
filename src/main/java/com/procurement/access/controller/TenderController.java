package com.procurement.access.controller;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.service.TenderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
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

    @PutMapping("/updateStatus")
    public ResponseEntity<ResponseDto> updateStatus(@RequestParam("cpId") final String cpId,
                                                    @RequestParam("status") final String status) {
        return new ResponseEntity<>(tenderService.updateStatus(cpId, status), HttpStatus.OK);
    }

    @PutMapping("/updateStatusDetails")
    public ResponseEntity<ResponseDto> updateStatusDetails(@RequestParam("cpId") final String cpId,
                                                           @RequestParam("statusDetails") final String statusDetails) {
        return new ResponseEntity<>(tenderService.updateStatusDetails(cpId, statusDetails), HttpStatus.OK);
    }

}
