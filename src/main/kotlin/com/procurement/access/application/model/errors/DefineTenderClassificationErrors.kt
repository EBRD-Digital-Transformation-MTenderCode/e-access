package com.procurement.access.application.model.errors

import com.procurement.access.domain.fail.error.CommandValidationErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid

object DefineTenderClassificationErrors {

    class NoHomogeneousItems : CommandValidationErrors(
        numberError = "1.52.1",
        description = "No item classifications in request homogenized to plan."
    )

    class MultiScheme : CommandValidationErrors(
        numberError = "1.52.2",
        description = "Items from request contains more than 1 scheme."
    )

    class RecordNotFound(cpid: Cpid, ocid: Ocid) : CommandValidationErrors(
        numberError = "1.52.3",
        description = "Record not found by cpid '$cpid' and ocid '$ocid'."
    )

}

