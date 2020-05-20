package com.procurement.access.application.service.criteria

import com.procurement.access.infrastructure.dto.cn.CnOnPnRequest
import com.procurement.access.service.command.checkActualItemRelation
import com.procurement.access.service.command.checkArrays
import com.procurement.access.service.command.checkAwardCriteriaDetailsAreRequired
import com.procurement.access.service.command.checkAwardCriteriaDetailsEnum
import com.procurement.access.service.command.checkAwardCriteriaEnum
import com.procurement.access.service.command.checkCastCoefficient
import com.procurement.access.service.command.checkCoefficient
import com.procurement.access.service.command.checkCoefficientDataType
import com.procurement.access.service.command.checkCoefficientRelatedOption
import com.procurement.access.service.command.checkCoefficientValueUniqueness
import com.procurement.access.service.command.checkConversionRelatesToEnum
import com.procurement.access.service.command.checkConversionRelation
import com.procurement.access.service.command.checkConversionWithoutCriteria
import com.procurement.access.service.command.checkCriteriaAndConversionAreRequired
import com.procurement.access.service.command.checkCriteriaWithAwardCriteria
import com.procurement.access.service.command.checkDatatypeCompliance
import com.procurement.access.service.command.checkDateTime
import com.procurement.access.service.command.checkMinMaxValue
import com.procurement.access.service.command.checkRequirements

interface CriteriaService {
    fun checkCriteria(data: CnOnPnRequest)
}

class CriteriaServiceImpl : CriteriaService {

    override fun checkCriteria(data: CnOnPnRequest) {

        data.checkConversionWithoutCriteria()
            .checkAwardCriteriaDetailsAreRequired()  // FReq-1.1.1.22
            .checkCriteriaAndConversionAreRequired() // FReq-1.1.1.23
            .checkCoefficientValueUniqueness()       // FReq-1.1.1.24
            .checkCriteriaWithAwardCriteria()        // FReq-1.1.1.27

            .checkActualItemRelation()   // FReq-1.1.1.3
            .checkDatatypeCompliance()   // FReq-1.1.1.4
            .checkMinMaxValue()          // FReq-1.1.1.5
            .checkDateTime()             // FReq-1.1.1.6
            .checkRequirements()         // FReq-1.1.1.8

            .checkConversionRelation()         // FReq-1.1.1.9  & FReq-1.1.1.10
            .checkCoefficient()                // FReq-1.1.1.11
            .checkCoefficientDataType()        // FReq-1.1.1.12
            .checkCastCoefficient()            // FReq-1.1.1.13
            .checkCoefficientRelatedOption()   // FReq-1.1.1.28
            .checkConversionRelatesToEnum()    // FReq-1.1.1.14
            .checkAwardCriteriaEnum()          // FReq-1.1.1.15
            .checkAwardCriteriaDetailsEnum()   // FReq-1.1.1.15
            .checkArrays()                     // FReq-1.1.1.16
    }

}
