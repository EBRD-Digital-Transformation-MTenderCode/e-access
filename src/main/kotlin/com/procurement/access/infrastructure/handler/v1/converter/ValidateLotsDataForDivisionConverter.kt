package com.procurement.access.infrastructure.handler.v1.converter

import com.procurement.access.application.model.notEmptyRule
import com.procurement.access.application.model.params.ValidateLotsDataForDivisionParams
import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.model.parseDate
import com.procurement.access.application.model.parseOcid
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.infrastructure.handler.v2.model.request.ValidateLotsDataForDivisionRequest
import com.procurement.access.lib.extension.mapResult
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.asSuccess
import com.procurement.access.lib.functional.flatMap
import com.procurement.access.lib.functional.validate

fun ValidateLotsDataForDivisionRequest.convert(): Result<ValidateLotsDataForDivisionParams, DataErrors> {

    val cpid = parseCpid(cpid)
        .onFailure { return it }

    val ocid = parseOcid(ocid)
        .onFailure { return it }

    val tender = tender.convert("tender")
        .onFailure { return it }

    return ValidateLotsDataForDivisionParams(
        cpid = cpid,
        ocid = ocid,
        tender = tender
    ).asSuccess()
}

private fun ValidateLotsDataForDivisionRequest.Tender.convert(path: String): Result<ValidateLotsDataForDivisionParams.Tender, DataErrors> {
    val lots = lots.validate(notEmptyRule("$path.lots"))
        .flatMap { it.mapResult { lot -> lot.convert("lots") } }
        .onFailure { return it }

    val items = items.validate(notEmptyRule("$path.items"))
        .onFailure { return it }
        .map { item -> item.convert()  }

    return ValidateLotsDataForDivisionParams.Tender(
        lots = lots,
        items = items
    ).asSuccess()
}

private fun ValidateLotsDataForDivisionRequest.Tender.Item.convert() =
    ValidateLotsDataForDivisionParams.Tender.Item(
        id = id,
        relatedLot = relatedLot
    )

private fun ValidateLotsDataForDivisionRequest.Tender.Lot.convert(path: String): Result<ValidateLotsDataForDivisionParams.Tender.Lot, DataErrors> {
    val contractPeriod = contractPeriod?.convert("$path.contractPeriod")
        ?.onFailure { return it }

    val options = options.validate(notEmptyRule("$path/options"))
        .flatMap { it.orEmpty().mapResult { option -> option.convert("$path/options") } }
        .onFailure { return it }

    return ValidateLotsDataForDivisionParams.Tender.Lot(
        id = id,
        internalId = internalId,
        title = title,
        description = description,
        value = value?.convert(),
        contractPeriod = contractPeriod,
        placeOfPerformance = placeOfPerformance?.convert(),
        hasRecurrence = hasRecurrence,
        hasOptions = hasOptions,
        hasRenewal = hasRenewal,
        recurrence = recurrence?.convert("$path.recurrence")?.onFailure { return it },
        renewal = renewal?.convert("$path.renewal")?.onFailure { return it },
        options = options
    ).asSuccess()
}

private fun ValidateLotsDataForDivisionRequest.Tender.Lot.Renewal.convert(path: String): Result<ValidateLotsDataForDivisionParams.Tender.Lot.Renewal, DataErrors> =
     ValidateLotsDataForDivisionParams.Tender.Lot.Renewal(
        description = description,
        minimumRenewals = minimumRenewals,
        maximumRenewals = maximumRenewals,
        period = period?.convert("$path.period")?.onFailure { return it }
    ).asSuccess()

private fun ValidateLotsDataForDivisionRequest.Tender.Lot.Renewal.Period.convert(path: String): Result<ValidateLotsDataForDivisionParams.Tender.Lot.Renewal.Period, DataErrors> {
    val startDate = startDate?.let { parseDate(startDate, "$path.startDate") }
        ?.onFailure { return it }

    val endDate = endDate?.let { parseDate(endDate, "$path.endDate") }
        ?.onFailure { return it }

    val maxExtentDate = maxExtentDate?.let { parseDate(maxExtentDate, "$path.maxExtentDate") }
        ?.onFailure { return it }

    return ValidateLotsDataForDivisionParams.Tender.Lot.Renewal.Period(
        startDate = startDate,
        endDate = endDate,
        maxExtentDate = maxExtentDate,
        durationInDays = durationInDays
    ).asSuccess()
}

private fun ValidateLotsDataForDivisionRequest.Tender.Lot.Recurrence.convert(path: String): Result<ValidateLotsDataForDivisionParams.Tender.Lot.Recurrence, DataErrors> {
    return ValidateLotsDataForDivisionParams.Tender.Lot.Recurrence(
        description = description,
        dates = dates?.mapResult { it.convert("$path.dates") }?.onFailure { return it }
    ).asSuccess()
}

private fun ValidateLotsDataForDivisionRequest.Tender.Lot.Recurrence.Date.convert(path: String): Result<ValidateLotsDataForDivisionParams.Tender.Lot.Recurrence.Date, DataErrors> {
    val startDate = startDate?.let { parseDate(startDate, "$path.startDate") }
        ?.onFailure { return it }

    return ValidateLotsDataForDivisionParams.Tender.Lot.Recurrence.Date(
        startDate = startDate
    ).asSuccess()}

private fun ValidateLotsDataForDivisionRequest.Tender.Lot.Option.convert(path: String): Result<ValidateLotsDataForDivisionParams.Tender.Lot.Option, DataErrors> =
    ValidateLotsDataForDivisionParams.Tender.Lot.Option(
        description = description,
        period = period?.toDomain("$path/period")?.onFailure { return it }
    ).asSuccess()

private fun ValidateLotsDataForDivisionRequest.Tender.Lot.Option.Period.toDomain(path: String): Result<ValidateLotsDataForDivisionParams.Tender.Lot.Option.Period, DataErrors> {
    val startDate = startDate?.let { parseDate(startDate, "$path.startDate") }
        ?.onFailure { return it }

    val endDate = endDate?.let { parseDate(endDate, "$path.endDate") }
        ?.onFailure { return it }

    val maxExtentDate = maxExtentDate?.let { parseDate(maxExtentDate, "$path.maxExtentDate") }
        ?.onFailure { return it }

    return ValidateLotsDataForDivisionParams.Tender.Lot.Option.Period(
        startDate = startDate,
        endDate = endDate,
        maxExtentDate = maxExtentDate,
        durationInDays = durationInDays
    ).asSuccess()
}

private fun ValidateLotsDataForDivisionRequest.Tender.Lot.Value.convert() = ValidateLotsDataForDivisionParams.Tender.Lot.Value(
    amount = amount,
    currency = currency
)

private fun ValidateLotsDataForDivisionRequest.Tender.Lot.PlaceOfPerformance.convert(): ValidateLotsDataForDivisionParams.Tender.Lot.PlaceOfPerformance =
    ValidateLotsDataForDivisionParams.Tender.Lot.PlaceOfPerformance(
        description = description,
        address = ValidateLotsDataForDivisionParams.Tender.Lot.PlaceOfPerformance.Address(
            postalCode = address.postalCode,
            streetAddress = address.streetAddress,
            addressDetails = address.addressDetails.let { addressDetails ->
                ValidateLotsDataForDivisionParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails(
                    country = addressDetails.country.let { country ->
                        ValidateLotsDataForDivisionParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Country(
                            scheme = country.scheme,
                            id = country.id,
                            description = country.description
                        )
                    },
                    region = addressDetails.region.let { region ->
                        ValidateLotsDataForDivisionParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Region(
                            scheme = region.scheme,
                            id = region.id,
                            description = region.description
                        )
                    },
                    locality = addressDetails.locality.let { locality ->
                        ValidateLotsDataForDivisionParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Locality(
                            scheme = locality.scheme,
                            id = locality.id,
                            description = locality.description
                        )
                    }
                )
            }
        )
    )

private fun ValidateLotsDataForDivisionRequest.Tender.Lot.ContractPeriod.convert(path: String): Result<ValidateLotsDataForDivisionParams.Tender.Lot.ContractPeriod, DataErrors> {
    val startDate = parseDate(startDate, "$path.startDate")
        .onFailure { return it }

    val endDate = parseDate(endDate, "$path.endDate")
        .onFailure { return it }

    return ValidateLotsDataForDivisionParams.Tender.Lot.ContractPeriod(
        startDate = startDate,
        endDate = endDate
    ).asSuccess()
}
