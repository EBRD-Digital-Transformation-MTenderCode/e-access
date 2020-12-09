package com.procurement.access.infrastructure.handler.v1.converter

import com.procurement.access.application.model.organization.GetOrganization
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.infrastructure.handler.v2.model.request.GetOrganizationRequest
import com.procurement.access.lib.functional.Result

fun GetOrganizationRequest.convert(): Result<GetOrganization.Params, DataErrors> =
    GetOrganization.Params.tryCreate(cpid = this.cpid, ocid = this.ocid, role = this.role)

