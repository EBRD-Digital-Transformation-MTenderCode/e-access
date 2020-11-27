package com.procurement.access.infrastructure.dto.converter

import com.procurement.access.application.model.params.ValidateClassificationParams
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.infrastructure.handler.v2.model.request.ValidateClassificationRequest
import com.procurement.access.lib.functional.Result

fun ValidateClassificationRequest.convert(): Result<ValidateClassificationParams, DataErrors> =
    ValidateClassificationParams.tryCreate(
        cpid = cpid,
        ocid = ocid,
        tender = ValidateClassificationParams.Tender(ValidateClassificationParams.Tender.Classification(tender.classification.id))
    )
