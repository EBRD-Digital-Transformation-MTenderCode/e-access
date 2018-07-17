package com.procurement.access.controller

import com.procurement.access.model.bpe.ResponseDto
import com.procurement.access.model.dto.pn.Pn
import com.procurement.access.service.PnService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import javax.validation.Valid

@Validated
@RestController
@RequestMapping("/pn")
class PnController(private val pnService: PnService) {

    @PostMapping
    fun createPn(@RequestParam("stage") stage: String,
                 @RequestParam("country") country: String,
                 @RequestParam(value = "pmd", required = false) pmd: String,
                 @RequestParam("owner") owner: String,
                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                 @RequestParam("date")
                 dateTime: LocalDateTime,
                 @Valid @RequestBody data: Pn): ResponseEntity<ResponseDto> {
        return ResponseEntity(
                pnService.createPn(
                        stage = stage,
                        country = country,
                        owner = owner,
                        dateTime = dateTime,
                        pn = data),
                HttpStatus.CREATED)
    }
}
