package com.procurement.access.infrastructure.handler.v2.converter

import com.procurement.access.application.model.notEmptyRule
import com.procurement.access.application.model.params.CreateRfqParams
import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.model.parseDate
import com.procurement.access.application.model.parseOcid
import com.procurement.access.application.model.parseOwner
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.infrastructure.handler.v2.model.request.CreateRfqRequest
import com.procurement.access.lib.extension.mapResult
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.asSuccess
import com.procurement.access.lib.functional.flatMap
import com.procurement.access.lib.functional.validate

fun CreateRfqRequest.convert(): Result<CreateRfqParams, DataErrors> =
    CreateRfqParams(
        cpid = parseCpid(cpid).onFailure { return it },
        relatedCpid = parseCpid(relatedCpid).onFailure { return it },
        relatedOcid = parseOcid(relatedOcid).onFailure { return it },
        additionalCpid = parseCpid(additionalCpid).onFailure { return it },
        additionalOcid = parseOcid(additionalOcid).onFailure { return it },
        date = parseDate(date, "date").onFailure { return it },
        owner = parseOwner(owner).onFailure { return it },
        tender = tender.convert("tender").onFailure { return it }
    ).asSuccess()

private fun CreateRfqRequest.Tender.convert(path: String): Result<CreateRfqParams.Tender, DataErrors> {
    val lots = lots.validate(notEmptyRule("$path/lots"))
        .flatMap { lots -> lots.mapResult { it.convert("$path/lots") } }
        .onFailure { return it }

    val items = items.validate(notEmptyRule("$path/items"))
        .flatMap { items -> items.mapResult { it.convert("$path/items") } }
        .onFailure { return it }

    val procurementMethodModalities = procurementMethodModalities
        .validate(notEmptyRule("$path/procurementMethodModalities"))
        .onFailure { return it }


    return CreateRfqParams.Tender(
        lots = lots,
        items = items,
        electronicAuctions = electronicAuctions?.convert("$path.electronicAuctions")?.onFailure { return it },
        procurementMethodModalities = procurementMethodModalities
    ).asSuccess()
}

private fun CreateRfqRequest.Tender.ElectronicAuctions.convert(path: String): Result<CreateRfqParams.Tender.ElectronicAuctions, DataErrors> {
    val details = details.validate(notEmptyRule("$path/details"))
        .flatMap { items -> items.mapResult { it.convert("$path/details") } }
        .onFailure { return it }

    return CreateRfqParams.Tender.ElectronicAuctions(
        details = details
    ).asSuccess()
}

private fun CreateRfqRequest.Tender.ElectronicAuctions.Detail.convert(path: String): Result<CreateRfqParams.Tender.ElectronicAuctions.Detail, DataErrors> =
    CreateRfqParams.Tender.ElectronicAuctions.Detail(
        id = id,
        relatedLot = relatedLot
    ).asSuccess()

private fun CreateRfqRequest.Tender.Item.convert(path: String): Result<CreateRfqParams.Tender.Item, DataErrors> =
    CreateRfqParams.Tender.Item(
        id = id,
        internalId = internalId,
        description = description,
        relatedLot = relatedLot,
        classification = CreateRfqParams.Tender.Item.Classification(
            id = classification.id,
            description = classification.description,
            scheme = classification.scheme
        ),
        quantity = quantity,
        unit = CreateRfqParams.Tender.Item.Unit(
            id = unit.id,
            name = unit.name
        )
    ).asSuccess()

private fun CreateRfqRequest.Tender.Lot.convert(path: String): Result<CreateRfqParams.Tender.Lot, DataErrors> {
    return CreateRfqParams.Tender.Lot(
        id = id,
        description = description,
        internalId = internalId,
        title = title,
        value = value.let { value -> CreateRfqParams.Tender.Lot.Value(currency = value.currency) },
        contractPeriod = contractPeriod.convert("$path.contractPeriod").onFailure { return it },
        placeOfPerformance = placeOfPerformance.convert()
    ).asSuccess()
}

private fun CreateRfqRequest.Tender.Lot.PlaceOfPerformance.convert() =
    CreateRfqParams.Tender.Lot.PlaceOfPerformance(
        description = description,
        address = address
            .let { address ->
                CreateRfqParams.Tender.Lot.PlaceOfPerformance.Address(
                    streetAddress = address.streetAddress,
                    postalCode = address.postalCode,
                    addressDetails = address.addressDetails.let { addressDetails ->
                        CreateRfqParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails(
                            country = addressDetails.country.let { country ->
                                CreateRfqParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Country(
                                    scheme = country.scheme,
                                    id = country.id,
                                    description = country.description,
                                    uri = country.uri
                                )
                            },
                            region = addressDetails.region.let { region ->
                                CreateRfqParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Region(
                                    scheme = region.scheme,
                                    id = region.id,
                                    description = region.description,
                                    uri = region.uri
                                )
                            },
                            locality = addressDetails.locality.let { locality ->
                                CreateRfqParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Locality(
                                    scheme = locality.scheme,
                                    id = locality.id,
                                    description = locality.description,
                                    uri = locality.uri
                                )
                            }

                        )
                    }
                )
            }

    )

private fun CreateRfqRequest.Tender.Lot.ContractPeriod.convert(path: String): Result<CreateRfqParams.Tender.Lot.ContractPeriod, DataErrors> =
    CreateRfqParams.Tender.Lot.ContractPeriod(
        startDate = parseDate(startDate, "$path.startDate").onFailure { return it },
        endDate = parseDate(endDate, "$path.endDate").onFailure { return it }
    ).asSuccess()
