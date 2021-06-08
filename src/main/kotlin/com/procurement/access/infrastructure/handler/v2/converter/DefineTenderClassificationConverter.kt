package com.procurement.access.infrastructure.handler.v2.converter

import com.procurement.access.application.model.params.DefineTenderClassificationParams
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.enums.Scheme
import com.procurement.access.infrastructure.handler.v2.model.request.DefineTenderClassificationRequest
import com.procurement.access.lib.extension.mapResult
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.asSuccess

fun DefineTenderClassificationRequest.convert(): Result<DefineTenderClassificationParams, DataErrors> {
    val convertedTender = tender.convert().onFailure { return it }
    return DefineTenderClassificationParams.tryCreate(relatedCpid, relatedOcid, convertedTender)
}

fun DefineTenderClassificationRequest.Tender.convert(): Result<DefineTenderClassificationParams.Tender, DataErrors> {
    val convertedItems = items.mapResult { it.convert() }
        .onFailure { return it }

    return DefineTenderClassificationParams.Tender(convertedItems).asSuccess()
}


fun DefineTenderClassificationRequest.Tender.Item.convert(): Result<DefineTenderClassificationParams.Tender.Item, DataErrors> =
    DefineTenderClassificationParams.Tender.Item(id, classification.convert()).asSuccess()


fun DefineTenderClassificationRequest.Tender.Item.Classification.convert(): DefineTenderClassificationParams.Tender.Item.Classification =
    DefineTenderClassificationParams.Tender.Item.Classification(id, Scheme.creator(scheme))

