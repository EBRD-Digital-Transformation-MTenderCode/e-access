package com.procurement.access.application.model.errors

import com.procurement.access.domain.fail.error.CommandValidationErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.RelatedProcessType

object GetBuyersOwnersErrors {

    class FeRecordNotFound(cpid: Cpid, ocid: Ocid.SingleStage) : CommandValidationErrors(
        numberError = "1.48.1",
        description = "FE record not found by cpid='$cpid' and ocid='$ocid'."
    )

    class MissingAggregatePlanningRelationship() : CommandValidationErrors(
        numberError = "1.48.2",
        description = "Relationship '${RelatedProcessType.AGGREGATE_PLANNING}' not found."
    )

    class ApRecordNotFound(cpid: Cpid, ocid: Ocid.SingleStage) : CommandValidationErrors(
        numberError = "1.48.3",
        description = "AP record not found by cpid='$cpid' and ocid='$ocid'."
    )

    class MissingXScopeRelationship() : CommandValidationErrors(
        numberError = "1.48.4",
        description = "Relationship '${RelatedProcessType.X_SCOPE}' not found."
    )

    class PnRecordNotFound(cpid: Cpid, ocid: Ocid.SingleStage) : CommandValidationErrors(
        numberError = "1.48.5",
        description = "PN record not found by cpid='$cpid' and ocid='$ocid'."
    )

}

