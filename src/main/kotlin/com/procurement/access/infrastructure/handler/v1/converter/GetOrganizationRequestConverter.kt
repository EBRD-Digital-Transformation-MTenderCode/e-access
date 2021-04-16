package com.procurement.access.infrastructure.handler.v1.converter

import com.procurement.access.application.model.organization.GetOrganizations
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.infrastructure.handler.v2.model.request.GetOrganizationsRequest
import com.procurement.access.lib.functional.Result

fun GetOrganizationsRequest.convert(): Result<GetOrganizations.Params, DataErrors> =
    GetOrganizations.Params.tryCreate(cpid = this.cpid, ocid = this.ocid, role = this.role)

