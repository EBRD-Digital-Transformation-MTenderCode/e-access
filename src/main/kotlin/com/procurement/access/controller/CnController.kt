package com.procurement.access.controller

import com.procurement.access.model.bpe.ResponseDto
import com.procurement.access.model.dto.cn.CnCreate
import com.procurement.access.model.dto.cn.CnUpdate
import com.procurement.access.service.CnCreateService
import com.procurement.access.service.CnUpdateService
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
class CnController(private val cnCreateService: CnCreateService,
                   private val cnUpdateService: CnUpdateService) {

//    @PostMapping
//    fun createCn(@RequestParam("stage") stage: String,
//                 @RequestParam("country") country: String,
//                 @RequestParam("pmd") pmd: String,
//                 @RequestParam("owner") owner: String,
//                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
//                 @RequestParam("date") dateTime: LocalDateTime,
//                 @Valid @RequestBody data: CnCreate): ResponseEntity<ResponseDto> {
//
//        return ResponseEntity(
//                cnCreateService.createCn(
//                        stage = stage,
//                        country = country,
//                        pmd = pmd,
//                        owner = owner,
//                        dateTime = dateTime,
//                        cnDto = data),
//                HttpStatus.CREATED)
//    }
//
//    @PutMapping
//    fun updateCn(@RequestParam("cpid") cpId: String,
//                 @RequestParam("stage") stage: String,
//                 @RequestParam("owner") owner: String,
//                 @RequestParam("token") token: String,
//                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
//                 @RequestParam("date") dateTime: LocalDateTime,
//                 @Valid @RequestBody data: CnUpdate): ResponseEntity<ResponseDto> {
//
//        return ResponseEntity(
//                cnUpdateService.updateCn(
//                        cpId = cpId,
//                        stage = stage,
//                        owner = owner,
//                        token = token,
//                        dateTime = dateTime,
//                        cnDto = data),
//                HttpStatus.CREATED)
//    }
}
