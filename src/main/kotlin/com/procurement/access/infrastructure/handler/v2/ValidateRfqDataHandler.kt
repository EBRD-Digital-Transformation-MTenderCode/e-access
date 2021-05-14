package com.procurement.access.infrastructure.handler.v2

import com.procurement.access.application.service.Logger
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.exception.EmptyStringException
import com.procurement.access.infrastructure.api.v2.CommandTypeV2
import com.procurement.access.infrastructure.handler.v2.base.AbstractValidationHandlerV2
import com.procurement.access.infrastructure.handler.v2.converter.convert
import com.procurement.access.infrastructure.handler.v2.model.request.ValidateRfqDataRequest
import com.procurement.access.lib.errorIfBlank
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.ValidationResult
import com.procurement.access.lib.functional.asFailure
import com.procurement.access.lib.functional.asSuccess
import com.procurement.access.lib.functional.asValidationFailure
import com.procurement.access.lib.functional.flatMap
import com.procurement.access.service.RfqService
import org.springframework.stereotype.Service

@Service
class ValidateRfqDataHandler(
    private val rfqService: RfqService,
    logger: Logger
) : AbstractValidationHandlerV2(logger) {

    override val action: CommandTypeV2
        get() = CommandTypeV2.VALIDATE_RFQ_DATA

    override fun execute(descriptor: CommandDescriptor): ValidationResult<Fail> {
        val params = descriptor.body.asJsonNode
            .params<ValidateRfqDataRequest>()
            .flatMap { it.validateTextAttributes() }
            .flatMap { it.convert() }
            .onFailure { return it.reason.asValidationFailure() }
        return rfqService.validateRfqData(params = params)
    }

    private fun ValidateRfqDataRequest.validateTextAttributes(): Result<ValidateRfqDataRequest, DataErrors.Validation.EmptyString> {
        try {
            tender.apply {
                title.checkForBlank("tender.title")
                description.checkForBlank("tender.description")

                lots.forEachIndexed { lotIdx, lot ->
                    lot.apply {
                        internalId.checkForBlank("tender.lots[$lotIdx].internalId")
                        title.checkForBlank("tender.lots[$lotIdx].title")
                        description.checkForBlank("tender.lots[$lotIdx].description")

                        placeOfPerformance.apply {
                            description.checkForBlank("tender.lots[$lotIdx].placeOfPerformance.description")

                            address.apply {
                                streetAddress.checkForBlank("tender.lots[$lotIdx].placeOfPerformance.address.streetAddress")
                                postalCode.checkForBlank("tender.lots[$lotIdx].placeOfPerformance.address.postalCode")

                                addressDetails.apply {
                                    country.apply {
                                        id.checkForBlank("tender.lots[$lotIdx].placeOfPerformance.address.addressDetails.country.id")
                                        scheme.checkForBlank("tender.lots[$lotIdx].placeOfPerformance.address.addressDetails.country.scheme")
                                        description.checkForBlank("tender.lots[$lotIdx].placeOfPerformance.address.addressDetails.country.description")
                                    }

                                    region.apply {
                                        id.checkForBlank("tender.lots[$lotIdx].placeOfPerformance.address.addressDetails.region.id")
                                        scheme.checkForBlank("tender.lots[$lotIdx].placeOfPerformance.address.addressDetails.region.scheme")
                                        description.checkForBlank("tender.lots[$lotIdx].placeOfPerformance.address.addressDetails.region.description")
                                    }

                                    locality.apply {
                                        id.checkForBlank("tender.lots[$lotIdx].placeOfPerformance.address.addressDetails.locality.id")
                                        scheme.checkForBlank("tender.lots[$lotIdx].placeOfPerformance.address.addressDetails.locality.scheme")
                                        description.checkForBlank("tender.lots[$lotIdx].placeOfPerformance.address.addressDetails.locality.description")
                                    }
                                }
                            }
                        }
                    }
                }

                items.forEachIndexed { itemIdx, item ->
                    item.apply {
                        internalId.checkForBlank("tender.items[$itemIdx].internalId")
                        description.checkForBlank("tender.items[$itemIdx].description")

                        classification.apply {
                            id.checkForBlank("tender.items[$itemIdx].classification.id")
                            scheme.checkForBlank("tender.items[$itemIdx].classification.scheme")
                        }
                        unit.id.checkForBlank("tender.items[$itemIdx].unit.id")
                        relatedLot.checkForBlank("tender.items[$itemIdx].relatedLot")
                    }
                }

                electronicAuctions?.details
                    ?.forEachIndexed { detailIdx, detail ->
                        detail.apply {
                            id.checkForBlank("tender.electronicAuctions.details[$detailIdx].description")
                            relatedLot.checkForBlank("tender.electronicAuctions.details[$detailIdx].relatedLot")
                        }
                    }
            }
        } catch (exception: EmptyStringException) {
            return DataErrors.Validation.EmptyString(exception.path).asFailure()
        }

        return this.asSuccess()
    }

    private fun String?.checkForBlank(path: String) = this.errorIfBlank { EmptyStringException(path) }
}