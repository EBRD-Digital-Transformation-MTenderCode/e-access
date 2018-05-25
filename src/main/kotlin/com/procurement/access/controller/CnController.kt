package com.procurement.access.controller

import com.procurement.access.model.dto.cn.CnProcess
import com.procurement.access.service.CnService
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
@RequestMapping("/cn")
class CnController(private val cnService: CnService) {

    @PostMapping
    fun createCn(@RequestParam("stage") stage: String,
                 @RequestParam("country") country: String,
                 @RequestParam(value = "pmd", required = false) pmd: String,
                 @RequestParam("owner") owner: String,
                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                 @RequestParam("date") dateTime: LocalDateTime,
                 @Valid @RequestBody data: CnProcess): ResponseEntity<ResponseDto<*>> {

        return ResponseEntity(
                cnService.createCn(
                        stage = stage,
                        country = country,
                        owner = owner,
                        dateTime = dateTime,
                        cn = data),
                HttpStatus.CREATED)
    }
}
