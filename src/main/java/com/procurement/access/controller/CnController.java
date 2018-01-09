package com.procurement.access.controller;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.cn.CnDto;
import com.procurement.access.service.CnService;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseDto create(@RequestParam("country") final String country,
                              @RequestParam("pmd") final String pmd,
                              @RequestParam("stage") final String stage,
                              @RequestParam("owner") final String owner,
                              @Valid @RequestBody final CnDto cnDto) {
        return cnService.createCn(country, pmd, stage, owner, cnDto);
    }

}
