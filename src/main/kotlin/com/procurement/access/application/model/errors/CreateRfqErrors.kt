package com.procurement.access.application.model.errors

import com.procurement.access.domain.fail.error.CommandValidationErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid

object CreateRfqErrors {

    class RecordNotFound(cpid: Cpid, ocid: Ocid.SingleStage) : CommandValidationErrors(
        numberError = "1.45.1",
        description = "Record not found by cpid='$cpid' and ocid='$ocid'."
    )

}

