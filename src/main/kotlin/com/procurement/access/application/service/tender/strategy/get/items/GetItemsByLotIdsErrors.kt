package com.procurement.access.application.service.tender.strategy.get.items

import com.procurement.access.domain.fail.error.CommandValidationErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid

object GetItemsByLotIdsErrors {

    class RecordNotFound(cpid: Cpid, ocid: Ocid) : CommandValidationErrors(
        numberError = "1.41.1",
        description = "Record not found by cpid='$cpid' and ocid='$ocid'."
    )

    class ItemsNotFound(cpid: Cpid, ocid: Ocid, relatedLots: Collection<String>) : CommandValidationErrors(
        numberError = "1.41.2",
        description = "Record not found items by related lots ($relatedLots) by cpid='$cpid' and ocid='$ocid'."
    )

}

