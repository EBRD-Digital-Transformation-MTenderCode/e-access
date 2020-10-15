package com.procurement.access.application.model.params

import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.model.parseOcid
import com.procurement.access.application.model.parseProcurementMethodModalities
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.ProcurementMethodModalities
import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.asSuccess
import com.procurement.access.domain.util.extension.mapResult
import com.procurement.access.lib.toSetBy

class CheckExistenceSignAuctionParams private constructor(
    val cpid: Cpid,
    val ocid: Ocid,
    val tender: Tender?
) {

    companion object {
        fun tryCreate(
            cpid: String,
            ocid: String,
            tender: Tender?
        ): Result<CheckExistenceSignAuctionParams, DataErrors> {
            val cpidParsed = parseCpid(value = cpid)
                .orForwardFail { fail -> return fail }

            val ocidParsed = parseOcid(value = ocid)
                .orForwardFail { fail -> return fail }

            return CheckExistenceSignAuctionParams(cpid = cpidParsed, ocid = ocidParsed, tender = tender)
                .asSuccess()
        }
    }

    class Tender private constructor(
        val procurementMethodModalities: List<ProcurementMethodModalities>
    ) {
        companion object {
            private val allowedProcurementMethodModalities = ProcurementMethodModalities.allowedElements
                .filter {
                    when (it) {
                        ProcurementMethodModalities.ELECTRONIC_AUCTION,
                        ProcurementMethodModalities.REQUIRES_ELECTRONIC_CATALOGUE -> true
                    }
                }
                .toSetBy { it }

            fun tryCreate(
                procurementMethodModalities: List<String>
            ): Result<Tender, DataErrors> {
                val procurementMethodModalitiesResult = procurementMethodModalities.mapResult {
                    parseProcurementMethodModalities(
                        value = it,
                        allowedEnums = allowedProcurementMethodModalities,
                        attributeName = "tender.procurementMethodModalities"
                    )
                }.orForwardFail { fail -> return fail }

                return Tender(procurementMethodModalitiesResult)
                    .asSuccess()
            }
        }
    }
}
