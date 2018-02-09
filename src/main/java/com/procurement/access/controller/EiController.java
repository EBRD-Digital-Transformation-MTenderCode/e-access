package com.procurement.access.controller;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.ein.EiDto;
import com.procurement.access.service.EiService;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/ein")
public class EiController {

    private final EiService eiService;

    public EiController(final EiService eiService) {
        this.eiService = eiService;
    }

    @PostMapping
    public ResponseEntity<ResponseDto> create(final String owner,
                                              @Valid @RequestBody final EiDto eiDto) {
        return new ResponseEntity<>(eiService.createEi(owner, eiDto), HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<ResponseDto> update(final String cpId,
                                              final String owner,
                                              final String token,
                                              @Valid @RequestBody final EiDto eiDto) {
        return new ResponseEntity<>(eiService.updateEi(owner, cpId, token, eiDto), HttpStatus.OK);
    }
}
