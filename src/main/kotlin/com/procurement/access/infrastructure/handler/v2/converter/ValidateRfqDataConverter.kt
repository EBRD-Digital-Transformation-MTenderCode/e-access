package com.procurement.access.infrastructure.handler.v2.converter

import com.procurement.access.application.model.notEmptyRule
import com.procurement.access.application.model.params.ValidateRfqDataParams
import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.model.parseDate
import com.procurement.access.application.model.parseOcid
import com.procurement.access.application.model.parseProcurementMethodModalities
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.enums.ProcurementMethodModalities
import com.procurement.access.infrastructure.handler.v2.model.request.ValidateRfqDataRequest
import com.procurement.access.lib.extension.mapResult
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.asSuccess
import com.procurement.access.lib.functional.flatMap
import com.procurement.access.lib.functional.validate

fun ValidateRfqDataRequest.convert(): Result<ValidateRfqDataParams, DataErrors> =
    ValidateRfqDataParams(
        relatedCpid = parseCpid(relatedCpid).onFailure { return it },
        relatedOcid = parseOcid(relatedOcid).onFailure { return it },
        tender = tender.convert("tender").onFailure { return it }
    ).asSuccess()

private val allowedProcurementMethodModalities = ProcurementMethodModalities.values()
    .filter {
        when (it) {
            ProcurementMethodModalities.ELECTRONIC_AUCTION -> true
            ProcurementMethodModalities.REQUIRES_ELECTRONIC_CATALOGUE -> false
        }
    }
    .toSet()

private fun ValidateRfqDataRequest.Tender.convert(path: String): Result<ValidateRfqDataParams.Tender, DataErrors> {
    val lots = lots.validate(notEmptyRule("$path/lots"))
        .flatMap { lots -> lots.mapResult { it.convert("$path/lots") } }
        .onFailure { return it }

    val items = items.validate(notEmptyRule("$path/items"))
        .flatMap { items -> items.mapResult { it.convert() } }
        .onFailure { return it }

    val procurementMethodModalities = procurementMethodModalities
        .validate(notEmptyRule("$path/procurementMethodModalities"))
        .onFailure { return it }
        .orEmpty()
        .mapResult {
            parseProcurementMethodModalities(
                it, allowedProcurementMethodModalities, "$path.procurementMethodModalities"
            )
        }
        .onFailure { return it }

    return ValidateRfqDataParams.Tender(
        title = title,
        description = description,
        lots = lots,
        items = items,
        electronicAuctions = electronicAuctions?.convert("$path.electronicAuctions")?.onFailure { return it },
        procurementMethodModalities = procurementMethodModalities,
        tenderPeriod = ValidateRfqDataParams.Tender.TenderPeriod(
            parseDate(tenderPeriod.endDate, "$path.tenderPeriod.endDate").onFailure { return it }
        )
    ).asSuccess()
}

private fun ValidateRfqDataRequest.Tender.ElectronicAuctions.convert(path: String): Result<ValidateRfqDataParams.Tender.ElectronicAuctions, DataErrors> {
    val details = details.validate(notEmptyRule("$path/details"))
        .flatMap { items -> items.mapResult { it.convert() } }
        .onFailure { return it }

    return ValidateRfqDataParams.Tender.ElectronicAuctions(
        details = details
    ).asSuccess()
}

private fun ValidateRfqDataRequest.Tender.ElectronicAuctions.Detail.convert(): Result<ValidateRfqDataParams.Tender.ElectronicAuctions.Detail, DataErrors> =
    ValidateRfqDataParams.Tender.ElectronicAuctions.Detail(
        id = id,
        relatedLot = relatedLot
    ).asSuccess()

private fun ValidateRfqDataRequest.Tender.Item.convert(): Result<ValidateRfqDataParams.Tender.Item, DataErrors> =
    ValidateRfqDataParams.Tender.Item(
        id = id,
        internalId = internalId,
        description = description,
        relatedLot = relatedLot,
        classification = ValidateRfqDataParams.Tender.Item.Classification(
            id = classification.id,
            scheme = classification.scheme
        ),
        quantity = quantity,
        unit = ValidateRfqDataParams.Tender.Item.Unit(
            id = unit.id
        )
    ).asSuccess()

private fun ValidateRfqDataRequest.Tender.Lot.convert(path: String): Result<ValidateRfqDataParams.Tender.Lot, DataErrors> {
    return ValidateRfqDataParams.Tender.Lot(
        id = id,
        description = description,
        internalId = internalId,
        title = title,
        value = value.let { value -> ValidateRfqDataParams.Tender.Lot.Value(currency = value.currency) },
        contractPeriod = contractPeriod.convert("$path.contractPeriod").onFailure { return it },
        placeOfPerformance = placeOfPerformance.convert()
    ).asSuccess()
}

private fun ValidateRfqDataRequest.Tender.Lot.PlaceOfPerformance.convert() =
    ValidateRfqDataParams.Tender.Lot.PlaceOfPerformance(
        description = description,
        address = address
            .let { address ->
                ValidateRfqDataParams.Tender.Lot.PlaceOfPerformance.Address(
                    streetAddress = address.streetAddress,
                    postalCode = address.postalCode,
                    addressDetails = address.addressDetails.let { addressDetails ->
                        ValidateRfqDataParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails(
                            country = addressDetails.country.let { country ->
                                ValidateRfqDataParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Country(
                                    scheme = country.scheme,
                                    id = country.id,
                                    description = country.description
                                )
                            },
                            region = addressDetails.region.let { region ->
                                ValidateRfqDataParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Region(
                                    scheme = region.scheme,
                                    id = region.id,
                                    description = region.description
                                )
                            },
                            locality = addressDetails.locality.let { locality ->
                                ValidateRfqDataParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Locality(
                                    scheme = locality.scheme,
                                    id = locality.id,
                                    description = locality.description
                                )
                            }

                        )
                    }
                )
            }

    )

private fun ValidateRfqDataRequest.Tender.Lot.ContractPeriod.convert(path: String): Result<ValidateRfqDataParams.Tender.Lot.ContractPeriod, DataErrors> =
    ValidateRfqDataParams.Tender.Lot.ContractPeriod(
        startDate = parseDate(startDate, "$path.startDate").onFailure { return it },
        endDate = parseDate(endDate, "$path.endDate").onFailure { return it }
    ).asSuccess()

