package com.procurement.access.controller

import com.procurement.access.model.dto.cn.CnProcess
import com.procurement.access.service.CnOnPnService
import com.procurement.notice.model.bpe.ResponseDto
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import javax.validation.Valid

@Validated
@RestController
@RequestMapping("/cnOnPn")
class CnOnPnController(private val cnOnPnService: CnOnPnService) {

    @PostMapping
    fun createCnOnPn(@RequestParam("identifier") cpId: String,
                     @RequestParam("previousStage") previousStage: String,
                     @RequestParam("stage") stage: String,
                     @RequestParam(value = "country", required = false) country: String,
                     @RequestParam(value = "pmd", required = false) pmd: String,
                     @RequestParam("owner") owner: String,
                     @RequestParam("token") token: String,
                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                     @RequestParam("date") dateTime: LocalDateTime,
                     @Valid @RequestBody data: CnProcess): ResponseEntity<ResponseDto<*>> {
        return ResponseEntity(
                cnOnPnService.createCnOnPn(
                        cpId = cpId,
                        previousStage = previousStage,
                        stage = stage,
                        owner = owner,
                        token = token,
                        dateTime = dateTime,
                        cn = data),
                HttpStatus.CREATED)
    }
}
