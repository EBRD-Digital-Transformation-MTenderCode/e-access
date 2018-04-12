package com.procurement.access.controller;

import com.procurement.access.model.dto.bpe.ResponseDto;
import com.procurement.access.model.dto.pnToPin.PnToPinDto;
import com.procurement.access.service.PNService;
import com.procurement.access.service.PnToPinService;
import java.time.LocalDateTime;
import javax.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/pnToPin")
public class PnToPinController {

    private final PnToPinService pnToPinService;

    public PnToPinController(final PnToPinService pnToPinService) {
        this.pnToPinService = pnToPinService;
    }

    @PostMapping
    public ResponseEntity<ResponseDto> createPnToPin(@RequestParam("identifier") final String id,
                                                     @RequestParam("previousstage") final String previousstage,
                                                     @RequestParam("stage") final String stage,
                                                     @RequestParam("owner") final String owner,
                                                     @RequestParam("token") final String token,
                                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                     @RequestParam("date") final LocalDateTime dateTime,
                                                     @RequestParam("country") final String country,
                                                     @RequestParam("pmd") final String pmd,
                                                     @Valid @RequestBody final PnToPinDto data) {
        return new ResponseEntity<>(
            pnToPinService.createPinfromPn(id,previousstage,stage,owner,token,dateTime,data),
            HttpStatus.CREATED);
    }
}
