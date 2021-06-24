package com.procurement.access.application.model.errors

import com.procurement.access.domain.fail.error.CommandValidationErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid

object GetDataForContractErrors {

    class RecordNotFound(cpid: Cpid, ocid: Ocid) : CommandValidationErrors(
        numberError = "1.51.1",
        description = "Record not found by cpid='$cpid' and ocid='$ocid'."
    )

}

