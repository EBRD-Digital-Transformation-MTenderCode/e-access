package com.procurement.access.application.service.tender.strategy.prepare.cancellation

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.model.enums.TenderStatus
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType.DATA_NOT_FOUND
import com.procurement.access.exception.ErrorType.INVALID_DOCUMENT_TYPE
import com.procurement.access.exception.ErrorType.INVALID_OWNER
import com.procurement.access.exception.ErrorType.INVALID_TENDER_STATUS
import com.procurement.access.exception.ErrorType.INVALID_TENDER_STATUS_DETAILS
import com.procurement.access.exception.ErrorType.INVALID_TOKEN
import com.procurement.access.model.dto.ocds.DocumentType
import com.procurement.access.model.dto.ocds.TenderProcess
import com.procurement.access.model.dto.ocds.TenderStatusDetails
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.localNowUTC
import com.procurement.access.utils.toDate
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toObject

class PrepareCancellationStrategy(
    private val tenderProcessDao: TenderProcessDao
) {
    fun execute(context: PrepareCancellationContext, data: PrepareCancellationData): PreparedCancellationData {
        //VR-3.16.10
        checkDocumentType(data.amendments)

        val entity: TenderProcessEntity = tenderProcessDao.getByCpIdAndStage(context.cpid, context.stage)
            ?: throw ErrorException(DATA_NOT_FOUND)

        //VR-3.16.1
        checkOwner(context = context, entity = entity)

        //VR-3.16.2
        checkToken(context = context, entity = entity)

        val tenderProcess = toObject(TenderProcess::class.java, entity.jsonData)

        //VR-3.16.4
        checkTenderStatuses(tenderProcess)

        val updatedTenderProcess = tenderProcess.copy(
            tender = tenderProcess.tender.copy(
                //BR-3.16.1
                statusDetails = TenderStatusDetails.CANCELLATION
            )
        )

        tenderProcessDao.save(
            entity = entity.copy(
                createdDate = localNowUTC().toDate(),
                jsonData = toJson(updatedTenderProcess)
            )
        )

        return PreparedCancellationData(
            tender = PreparedCancellationData.Tender(
                statusDetails = updatedTenderProcess.tender.statusDetails
            )
        )
    }

    /**
     * VR-3.16.1
     */
    private fun checkOwner(context: PrepareCancellationContext, entity: TenderProcessEntity) {
        if (entity.owner != context.owner)
            throw ErrorException(error = INVALID_OWNER)
    }

    /**
     * VR-3.16.2
     */
    private fun checkToken(context: PrepareCancellationContext, entity: TenderProcessEntity) {
        if (entity.token != context.token)
            throw ErrorException(error = INVALID_TOKEN)
    }

    /**
     * VR-3.16.4 "status" "statusDetails" (tender)
     *
     * Analyzes the values of tender.status in saved tender object:
     * 1. IF tender.status == "active", analyzes the tender.statusDetails value:
     *   a. IF tender.statusDetails != "suspended"
     *      validation is successful;
     *   b. ELSE (tender.statusDetails == "suspended")
     *      Access throws Exception: "Suspended tender has to be unsuspended to provide cancellation";
     * 2. ELSE (tender.status != "active")
     *    eAccess throws Exception: "Tender can't be cancelled";
     */
    private fun checkTenderStatuses(tenderProcess: TenderProcess) {
        when (tenderProcess.tender.status) {
            TenderStatus.ACTIVE -> {
                when (tenderProcess.tender.statusDetails) {
                    TenderStatusDetails.TENDERING -> Unit
                    TenderStatusDetails.AWARDING -> Unit
                    TenderStatusDetails.CLARIFICATION -> Unit
                    TenderStatusDetails.NEGOTIATION -> Unit
                    TenderStatusDetails.SUSPENDED -> throw ErrorException(
                        error = INVALID_TENDER_STATUS_DETAILS,
                        message = "The suspended tender has to be active to provide cancellation."
                    )
                    else -> throw ErrorException(error = INVALID_TENDER_STATUS_DETAILS)
                }
            }
            else -> throw ErrorException(
                error = INVALID_TENDER_STATUS,
                message = "Tender can't be cancelled, because tender in ${tenderProcess.tender.status.value} status."
            )
        }
    }

    /**
     * VR-3.16.10 Documents.documentType (amendment)
     *
     * eAccess checks Documents.documentType values in Documents object from Request:
     * IF documents.documentType values == "cancellationDetails" || "conflictOfInterest"
     *    validation is successful;
     * ELSE
     *    eAccess throws Exception: "Invalid document type";
     */
    private fun checkDocumentType(amendments: List<PrepareCancellationData.Amendment>) {
        val isValid = amendments.asSequence()
            .flatMap {
                it.documents?.asSequence() ?: emptySequence()
            }
            .all { document ->
                isValidDocumentType(document)
            }


        if (!isValid) throw ErrorException(error = INVALID_DOCUMENT_TYPE)
    }

    private fun isValidDocumentType(document: PrepareCancellationData.Amendment.Document): Boolean =
        when (document.documentType) {
            DocumentType.CANCELLATION_DETAILS -> true
            else -> false
        }
}
