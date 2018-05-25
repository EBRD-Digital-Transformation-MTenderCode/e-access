package com.procurement.access.controller

import com.procurement.access.model.dto.pin.PinProcess
import com.procurement.access.service.PinOnPnService
import com.procurement.access.model.bpe.ResponseDto
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import javax.validation.Valid

@Validated
@RestController
@RequestMapping("/pinOnPn")
class PinOnPnController(private val pinOnPnService: PinOnPnService) {

    @PostMapping
    fun createPinOnPn(@RequestParam("identifier") cpId: String,
                      @RequestParam("token") token: String,
                      @RequestParam(value = "country", required = false) country: String,
                      @RequestParam(value = "pmd", required = false) pmd: String,
                      @RequestParam("owner") owner: String,
                      @RequestParam("stage") stage: String,
                      @RequestParam("previousStage") previousStage: String,
                      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                      @RequestParam("date")
                      dateTime: LocalDateTime,
                      @Valid @RequestBody data: PinProcess): ResponseEntity<ResponseDto<*>> {
        return ResponseEntity(
                pinOnPnService.createPinOnPn(
                        cpId = cpId,
                        token = token,
                        owner = owner,
                        stage = stage,
                        previousStage = previousStage,
                        dateTime = dateTime,
                        pin = data),
                HttpStatus.CREATED)
    }
}
