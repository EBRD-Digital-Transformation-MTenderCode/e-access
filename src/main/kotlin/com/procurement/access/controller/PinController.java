package com.procurement.access.controller;

import com.procurement.access.model.dto.pin.PinProcess;
import com.procurement.access.service.PINService;
import java.time.LocalDateTime;
import javax.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/pin")
public class PinController {

    private final PINService pinService;

    public PinController(final PINService pinService) {
        this.pinService = pinService;
    }

    @PostMapping
    public ResponseEntity<ResponseDto> createPin(@RequestParam("stage") final String stage,
                                                 @RequestParam("country") final String country,
                                                 @RequestParam(value = "pmd", required = false) final String pmd,
                                                 @RequestParam("owner") final String owner,
                                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                 @RequestParam("date") final LocalDateTime dateTime,
                                                 @Valid @RequestBody final PinProcess data) {
        return new ResponseEntity<>(
                pinService.createPin(stage, country, owner, dateTime, data),
                HttpStatus.CREATED);
    }
}
