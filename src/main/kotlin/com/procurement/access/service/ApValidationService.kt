package com.procurement.access.service

import com.procurement.access.application.model.context.CheckExistanceItemsAndLotsContext
import com.procurement.access.domain.model.enums.RelatedProcessType
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.entity.APEntity
import com.procurement.access.infrastructure.entity.process.RelatedProcess
import com.procurement.access.infrastructure.repository.CassandraTenderProcessRepositoryV1
import com.procurement.access.service.ApValidationServiceImpl.CheckExistanceItemsAndLots.hasRelationWithPn
import com.procurement.access.utils.toObject
import org.springframework.stereotype.Service

interface ApValidationService {
    fun checkExistanceItemsAndLots(context: CheckExistanceItemsAndLotsContext)
}

@Service
class ApValidationServiceImpl(private val tenderRepository: CassandraTenderProcessRepositoryV1) : ApValidationService {

    override fun checkExistanceItemsAndLots(context: CheckExistanceItemsAndLotsContext) {
        val cpid = context.cpid
        val ocid = context.ocid

        val apEntity: APEntity = tenderRepository.getByCpidAndOcid(cpid = cpid, ocid = ocid)
            ?.let { apEntity -> toObject(APEntity::class.java, apEntity.jsonData) }
            ?: throw ErrorException( // VR.COM-1.26.1
                error = ErrorType.ENTITY_NOT_FOUND,
                message = "Cannot found tender by cpid='${cpid}' and ocid='${ocid}'."
            )

        // VR.COM-1.26.2
        if (apEntity.tender.items == null || apEntity.tender.items.isEmpty())
            throw ErrorException(
                error = ErrorType.DATA_NOT_FOUND,
                message = "Cannot found items in tender (cpid='${cpid}' and ocid='${ocid}')"
            )

        // VR.COM-1.26.3
        if (apEntity.tender.lots == null || apEntity.tender.lots.isEmpty())
            throw ErrorException(
                error = ErrorType.DATA_NOT_FOUND,
                message = "Cannot found lots in tender (cpid='${cpid}' and ocid='${ocid}')"
            )

        // VR.COM-1.26.4
        if (!hasRelationWithPn(apEntity.relatedProcesses.orEmpty()))
            throw ErrorException(
                error = ErrorType.INVALID_RELATED_PROCESS_RELATIONSHIP,
                message = "Cannot found relation with PN in tender (cpid='${cpid}' and ocid='${ocid}')"
            )
    }

    object CheckExistanceItemsAndLots {
        fun hasRelationWithPn(relatedProcesses: List<RelatedProcess>): Boolean =
            relatedProcesses.any { process ->
                process.relationship.any { relationship -> relationship == RelatedProcessType.X_SCOPE }
            }
    }
}
