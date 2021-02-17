package com.procurement.access.infrastructure.handler.v1.converter

import com.procurement.access.application.model.notEmptyRule
import com.procurement.access.application.model.params.DivideLotParams
import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.model.parseDate
import com.procurement.access.application.model.parseOcid
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.infrastructure.handler.v2.model.request.DivideLotRequest
import com.procurement.access.lib.extension.mapResult
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.asSuccess
import com.procurement.access.lib.functional.flatMap
import com.procurement.access.lib.functional.validate

fun DivideLotRequest.convert(): Result<DivideLotParams, DataErrors> {
    val cpid = parseCpid(cpid)
        .onFailure { return it }
    val ocid = parseOcid(ocid)
        .onFailure { return it }
    val tender = tender.convert("tender")
        .onFailure { return it }

    return DivideLotParams(
        cpid = cpid,
        ocid = ocid,
        tender = tender
    ).asSuccess()
}

fun DivideLotRequest.Tender.convert(path: String): Result<DivideLotParams.Tender, DataErrors> {
    val lots = lots.validate(notEmptyRule("$path/lots"))
        .flatMap { lots -> lots.mapResult { it.convert("$path/lots") } }
        .onFailure { return it }

    val items = items.validate(notEmptyRule("$path/items"))
        .onFailure { return it }
        .map { item -> item.convert() }

    return DivideLotParams.Tender(
        lots = lots,
        items = items
    ).asSuccess()
}

fun DivideLotRequest.Tender.Lot.convert(path: String): Result<DivideLotParams.Tender.Lot, DataErrors> {
    val contractPeriod = contractPeriod?.convert("$path.contractPeriod")
        ?.onFailure { return it }

    val options = options.validate(notEmptyRule("$path/options"))
        .flatMap { it.orEmpty().mapResult { option -> option.convert("$path/options") } }
        .onFailure { return it }

    return DivideLotParams.Tender.Lot(
        id = id,
        internalId = internalId,
        description = description,
        title = title,
        contractPeriod = contractPeriod,
        value = value?.convert(),
        placeOfPerformance = placeOfPerformance?.convert(),
        hasRecurrence = hasRecurrence,
        hasOptions = hasOptions,
        hasRenewal = hasRenewal,
        recurrence = recurrence?.convert("$path.recurrence")?.onFailure { return it },
        renewal = renewal?.convert("$path.renewal")?.onFailure { return it },
        options = options
    ).asSuccess()
}

private fun DivideLotRequest.Tender.Lot.Renewal.convert(path: String): Result<DivideLotParams.Tender.Lot.Renewal, DataErrors> =
    DivideLotParams.Tender.Lot.Renewal(
        description = description,
        minimumRenewals = minimumRenewals,
        maximumRenewals = maximumRenewals,
        period = period?.convert("$path.period")?.onFailure { return it }
    ).asSuccess()

private fun DivideLotRequest.Tender.Lot.Renewal.Period.convert(path: String): Result<DivideLotParams.Tender.Lot.Renewal.Period, DataErrors> {
    val startDate = startDate?.let { parseDate(startDate, "$path.startDate") }
        ?.onFailure { return it }

    val endDate = endDate?.let { parseDate(endDate, "$path.endDate") }
        ?.onFailure { return it }

    val maxExtentDate = maxExtentDate?.let { parseDate(maxExtentDate, "$path.maxExtentDate") }
        ?.onFailure { return it }

    return DivideLotParams.Tender.Lot.Renewal.Period(
        startDate = startDate,
        endDate = endDate,
        maxExtentDate = maxExtentDate,
        durationInDays = durationInDays
    ).asSuccess()
}

private fun DivideLotRequest.Tender.Lot.Recurrence.convert(path: String): Result<DivideLotParams.Tender.Lot.Recurrence, DataErrors> {
    return DivideLotParams.Tender.Lot.Recurrence(
        description = description,
        dates = dates?.mapResult { it.convert("$path.dates") }?.onFailure { return it }
    ).asSuccess()
}

private fun DivideLotRequest.Tender.Lot.Recurrence.Date.convert(path: String): Result<DivideLotParams.Tender.Lot.Recurrence.Date, DataErrors> {
    val startDate = startDate?.let { parseDate(startDate, "$path.startDate") }
        ?.onFailure { return it }

    return DivideLotParams.Tender.Lot.Recurrence.Date(
        startDate = startDate
    ).asSuccess()}

private fun DivideLotRequest.Tender.Lot.Option.convert(path: String): Result<DivideLotParams.Tender.Lot.Option, DataErrors> =
    DivideLotParams.Tender.Lot.Option(
        description = description,
        period = period?.toDomain("$path/period")?.onFailure { return it }
    ).asSuccess()

private fun DivideLotRequest.Tender.Lot.Option.Period.toDomain(path: String): Result<DivideLotParams.Tender.Lot.Option.Period, DataErrors> {
    val startDate = startDate?.let { parseDate(startDate, "$path.startDate") }
        ?.onFailure { return it }

    val endDate = endDate?.let { parseDate(endDate, "$path.endDate") }
        ?.onFailure { return it }

    val maxExtentDate = maxExtentDate?.let { parseDate(maxExtentDate, "$path.maxExtentDate") }
        ?.onFailure { return it }

    return DivideLotParams.Tender.Lot.Option.Period(
        startDate = startDate,
        endDate = endDate,
        maxExtentDate = maxExtentDate,
        durationInDays = durationInDays
    ).asSuccess()
}

fun DivideLotRequest.Tender.Lot.Value.convert() = DivideLotParams.Tender.Lot.Value(amount, currency)

fun DivideLotRequest.Tender.Lot.ContractPeriod.convert(path: String): Result<DivideLotParams.Tender.Lot.ContractPeriod, DataErrors> {
    val startDate = parseDate(startDate, "$path.startDate")
        .onFailure { return it }
    val endDate = parseDate(endDate, "$path.endDate")
        .onFailure { return it }

    return DivideLotParams.Tender.Lot.ContractPeriod(startDate, endDate).asSuccess()
}

fun DivideLotRequest.Tender.Lot.PlaceOfPerformance.convert() = DivideLotParams.Tender.Lot.PlaceOfPerformance(
    description = description,
    address = DivideLotParams.Tender.Lot.PlaceOfPerformance.Address(
        streetAddress = address.streetAddress,
        postalCode = address.postalCode,
        addressDetails = address.addressDetails.let { addressDetails ->
            DivideLotParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails(
                country = addressDetails.country.let { country ->
                    DivideLotParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Country(
                        id = country.id,
                        description = country.description,
                        scheme = country.scheme,
                        uri = country.uri
                    )
                },
                region = addressDetails.region.let { region ->
                    DivideLotParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Region(
                        id = region.id,
                        description = region.description,
                        scheme = region.scheme,
                        uri = region.uri
                    )
                },
                locality = addressDetails.locality.let { locality ->
                    DivideLotParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Locality(
                        id = locality.id,
                        description = locality.description,
                        scheme = locality.scheme,
                        uri = locality.uri
                    )
                }
            )
        }
    )
)

fun DivideLotRequest.Tender.Item.convert() = DivideLotParams.Tender.Item(
    id = id,
    relatedLot = relatedLot
)


