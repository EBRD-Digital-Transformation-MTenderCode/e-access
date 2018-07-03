package com.procurement.access.controller

import com.procurement.access.model.bpe.ResponseDto
import com.procurement.access.model.dto.pin.PinProcess
import com.procurement.access.service.PinService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import javax.validation.Valid

@Validated
@RestController
@RequestMapping("/pin")
class PinController(private val pinService: PinService) {

    @PostMapping
    fun createPin(@RequestParam("stage") stage: String,
                  @RequestParam("country") country: String,
                  @RequestParam(value = "pmd", required = false) pmd: String,
                  @RequestParam("owner") owner: String,
                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                  @RequestParam("date") dateTime: LocalDateTime,
                  @Valid @RequestBody data: PinProcess): ResponseEntity<ResponseDto> {
        return ResponseEntity(
                pinService.createPin(
                        stage = stage,
                        country = country,
                        owner = owner,
                        dateTime = dateTime,
                        pin = data),
                HttpStatus.CREATED)
    }
}
