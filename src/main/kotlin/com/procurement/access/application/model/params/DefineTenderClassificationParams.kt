package com.procurement.access.application.model.params

import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.model.parseOcid
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.Scheme
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.asSuccess

class DefineTenderClassificationParams private constructor(
    val relatedCpid: Cpid,
    val relatedOcid: Ocid.SingleStage,
    val tender: Tender
) {
    companion object {
        fun tryCreate(relatedCpid: String, relatedOcid: String, tender: Tender): Result<DefineTenderClassificationParams, DataErrors> {
            val cpid = parseCpid(relatedCpid).onFailure { return it }
            val ocid = parseOcid(relatedOcid).onFailure { return it }

            return DefineTenderClassificationParams(cpid, ocid, tender).asSuccess()
        }
    }

    data class Tender (
        val items: List<Item>
    ) {
        data class Item(
            val id: String,
            val classification: Classification
        ) {
            data class Classification(
                val id: String,
                val scheme: Scheme
            )
        }
    }
}