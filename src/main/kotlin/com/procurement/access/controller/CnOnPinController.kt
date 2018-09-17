package com.procurement.access.controller

import com.procurement.access.model.bpe.ResponseDto
import com.procurement.access.model.dto.cn.CnUpdate
import com.procurement.access.service.CnOnPinService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import javax.validation.Valid

@Validated
@RestController
@RequestMapping("/cnOnPin")
class CnOnPinController(private val cnOnPinService: CnOnPinService) {

//    @PostMapping
//    fun createCnOnPin(@RequestParam("cpid") cpId: String,
//                       @RequestParam("previousStage") previousStage: String,
//                       @RequestParam("stage") stage: String,
//                       @RequestParam("owner") owner: String,
//                       @RequestParam("token") token: String,
//                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
//                       @RequestParam("date") dateTime: LocalDateTime,
//                       @Valid @RequestBody data: CnUpdate): ResponseEntity<ResponseDto> {
//        return ResponseEntity(
//                cnOnPinService.createCnOnPin(
//                        cpId = cpId,
//                        previousStage = previousStage,
//                        stage = stage,
//                        owner = owner,
//                        token = token,
//                        dateTime = dateTime,
//                        cnDto = data),
//                HttpStatus.CREATED)
//    }
}
