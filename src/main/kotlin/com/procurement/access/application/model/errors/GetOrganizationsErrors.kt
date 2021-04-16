package com.procurement.access.application.model.errors

import com.procurement.access.application.model.organization.GetOrganizations
import com.procurement.access.domain.fail.error.CommandValidationErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.Stage

object GetOrganizationsErrors {

    class TenderNotFound(val cpid: Cpid, val ocid: Ocid.SingleStage) : CommandValidationErrors(
        numberError = "1.9.1",
        description = "Tender not found by cpid '$cpid' and ocid '$ocid'."
    )

    class OrganizationByRoleNotFound(role: GetOrganizations.Params.OrganizationRole) : CommandValidationErrors(
        numberError = "1.9.2",
        description = "Cannot find organization by role '$role'."
    )

    class UnexpectedStage(stage: Stage) : CommandValidationErrors(
        numberError = "1.9.3",
        description = "Stage '${stage}' not allowed at this command."
    )

}

