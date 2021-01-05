package com.procurement.access.infrastructure.handler.v1.converter

import com.procurement.access.application.model.notEmptyRule
import com.procurement.access.application.model.params.ValidateLotsDataParams
import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.model.parseDate
import com.procurement.access.application.model.parseOcid
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.infrastructure.handler.v2.model.request.ValidateLotsDataRequest
import com.procurement.access.lib.extension.mapResult
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.asSuccess
import com.procurement.access.lib.functional.flatMap
import com.procurement.access.lib.functional.validate

fun ValidateLotsDataRequest.convert(): Result<ValidateLotsDataParams, DataErrors> {

    val cpid = parseCpid(cpid)
        .onFailure { return it }

    val ocid = parseOcid(ocid)
        .onFailure { return it }

    val tender = tender.convert("tender")
        .onFailure { return it }

    return ValidateLotsDataParams(
        cpid = cpid,
        ocid = ocid,
        tender = tender
    ).asSuccess()
}

private fun ValidateLotsDataRequest.Tender.convert(path: String): Result<ValidateLotsDataParams.Tender, DataErrors> {
    val lots = lots.validate(notEmptyRule("$path.lots"))
        .flatMap { it.mapResult { lot -> lot.convert("lots") } }
        .onFailure { return it }

    val items = items.validate(notEmptyRule("$path.items"))
        .onFailure { return it }
        .map { item -> item.convert()  }

    return ValidateLotsDataParams.Tender(
        lots = lots,
        items = items
    ).asSuccess()
}

private fun ValidateLotsDataRequest.Tender.Item.convert() =
    ValidateLotsDataParams.Tender.Item(
        id = id,
        relatedLot = relatedLot
    )

private fun ValidateLotsDataRequest.Tender.Lot.convert(path: String): Result<ValidateLotsDataParams.Tender.Lot, DataErrors> {
    val contractPeriod = contractPeriod?.convert("$path.contractPeriod")
        ?.onFailure { return it }

    return ValidateLotsDataParams.Tender.Lot(
        id = id,
        internalId = internalId,
        title = title,
        description = description,
        value = value?.convert(),
        contractPeriod = contractPeriod,
        placeOfPerformance = placeOfPerformance?.convert()

    ).asSuccess()
}

private fun ValidateLotsDataRequest.Tender.Lot.Value.convert() = ValidateLotsDataParams.Tender.Lot.Value(
    amount = amount,
    currency = currency
)

private fun ValidateLotsDataRequest.Tender.Lot.PlaceOfPerformance.convert(): ValidateLotsDataParams.Tender.Lot.PlaceOfPerformance =
    ValidateLotsDataParams.Tender.Lot.PlaceOfPerformance(
        description = description,
        address = ValidateLotsDataParams.Tender.Lot.PlaceOfPerformance.Address(
            postalCode = address.postalCode,
            streetAddress = address.streetAddress,
            addressDetails = address.addressDetails.let { addressDetails ->
                ValidateLotsDataParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails(
                    country = addressDetails.country.let { country ->
                        ValidateLotsDataParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Country(
                            scheme = country.scheme,
                            id = country.id,
                            description = country.description
                        )
                    },
                    region = addressDetails.region.let { region ->
                        ValidateLotsDataParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Region(
                            scheme = region.scheme,
                            id = region.id,
                            description = region.description
                        )
                    },
                    locality = addressDetails.locality.let { locality ->
                        ValidateLotsDataParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Locality(
                            scheme = locality.scheme,
                            id = locality.id,
                            description = locality.description
                        )
                    }
                )
            }
        )
    )

private fun ValidateLotsDataRequest.Tender.Lot.ContractPeriod.convert(path: String): Result<ValidateLotsDataParams.Tender.Lot.ContractPeriod, DataErrors> {
    val startDate = parseDate(startDate, "$path.startDate")
        .onFailure { return it }

    val endDate = parseDate(endDate, "$path.endDate")
        .onFailure { return it }

    return ValidateLotsDataParams.Tender.Lot.ContractPeriod(
        startDate = startDate,
        endDate = endDate
    ).asSuccess()
}
