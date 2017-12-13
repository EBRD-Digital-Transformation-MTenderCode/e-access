package com.procurement.access.controller;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.fs.FsDto;
import com.procurement.access.service.FsService;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/fs")
public class FsController {

    private final FsService fsService;

    public FsController(final FsService fsService) {
        this.fsService = fsService;
    }

    @PostMapping(value = "/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseDto create(@Valid @RequestBody final FsDto fsDto) {
        return fsService.createFs(fsDto);
    }
}
