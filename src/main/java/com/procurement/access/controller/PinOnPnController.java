package com.procurement.access.controller;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.pin.PinProcess;
import com.procurement.access.service.PinOnPnService;
import java.time.LocalDateTime;
import javax.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/pinOnPn")
public class PinOnPnController {

    private final PinOnPnService pinOnPnService;

    public PinOnPnController(final PinOnPnService pinOnPnService) {
        this.pinOnPnService = pinOnPnService;
    }

    @PostMapping
    public ResponseEntity<ResponseDto> createPinOnPn(@RequestParam("identifier") final String cpId,
                                                     @RequestParam("token") final String token,
                                                     @RequestParam("country") final String country,
                                                     @RequestParam("pmd") final String pmd,
                                                     @RequestParam("owner") final String owner,
                                                     @RequestParam("stage") final String stage,
                                                     @RequestParam("previousStage") final String previousStage,
                                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                     @RequestParam("date") final LocalDateTime dateTime,
                                                     @Valid @RequestBody final PinProcess data) {
        return new ResponseEntity<>(
                pinOnPnService.createPinOnPn(
                        cpId,
                        token,
                        owner,
                        stage,
                        previousStage,
                        dateTime,
                        data),
                HttpStatus.CREATED);
    }
}
