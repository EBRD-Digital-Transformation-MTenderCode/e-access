package com.procurement.access.controller;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.cn.CnDto;
import com.procurement.access.model.dto.lots.LotsRequestDto;
import com.procurement.access.service.CnService;
import com.procurement.access.service.LotsService;
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
    private final LotsService lotsService;

    public CnController(final CnService cnService,
                        final LotsService lotsService) {
        this.cnService = cnService;
        this.lotsService = lotsService;
    }

    @PostMapping
    public ResponseEntity<ResponseDto> create(@RequestParam("owner") final String owner,
                                              @Valid @RequestBody final CnDto cnDto) {
        return new ResponseEntity<>(cnService.createCn(owner, cnDto), HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<ResponseDto> update(@RequestParam("owner") final String owner,
                                              @RequestParam("cpId") final String cpId,
                                              @RequestParam("token") final String token,
                                              @Valid @RequestBody final CnDto cnDto) {
        return new ResponseEntity<>(cnService.updateCn(owner, cpId, token, cnDto), HttpStatus.OK);
    }

    @GetMapping("/lots")
    public ResponseEntity<ResponseDto> getLots(@RequestParam("cpId") final String cpId,
                                               @RequestParam("status") final String status) {
        return new ResponseEntity<>(lotsService.getLots(cpId, status), HttpStatus.OK);
    }

    @PutMapping("/lots/updateStatus")
    public ResponseEntity<ResponseDto> updateLotsStatus(@RequestParam("cpId") final String cpId,
                                                        @RequestParam("status") final String status,
                                                        @Valid @RequestBody final LotsRequestDto lotsDto) {
        return new ResponseEntity<>(lotsService.updateLotsStatus(cpId, status, lotsDto), HttpStatus.OK);
    }

    @PutMapping("/lots/updateStatusDetails")
    public ResponseEntity<ResponseDto> updateLotsStatusDetails(@RequestParam("cpId") final String cpId,
                                                               @RequestParam("statusDetails") final String statusDetails,
                                                               @Valid @RequestBody final LotsRequestDto lotsDto) {
        return new ResponseEntity<>(lotsService.updateLotsStatusDetails(cpId, statusDetails, lotsDto), HttpStatus.OK);
    }

}
