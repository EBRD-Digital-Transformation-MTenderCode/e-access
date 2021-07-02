package com.procurement.access.infrastructure.handler.v2

import com.procurement.access.application.model.params.DivideLotParams
import com.procurement.access.application.service.Logger
import com.procurement.access.application.service.Transform
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.exception.EmptyStringException
import com.procurement.access.infrastructure.api.v2.CommandTypeV2
import com.procurement.access.infrastructure.handler.HistoryRepositoryNew
import com.procurement.access.infrastructure.handler.HistoryRepositoryOld
import com.procurement.access.infrastructure.handler.v1.converter.convert
import com.procurement.access.infrastructure.handler.v2.base.AbstractHistoricalHandler
import com.procurement.access.infrastructure.handler.v2.model.request.DivideLotRequest
import com.procurement.access.infrastructure.handler.v2.model.response.DivideLotResult
import com.procurement.access.lib.errorIfBlank
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.asFailure
import com.procurement.access.lib.functional.asSuccess
import com.procurement.access.lib.functional.flatMap
import com.procurement.access.service.LotsService
import org.springframework.stereotype.Service

@Service
class DivideLotHandler(
    private val lotsService: LotsService,
    transform: Transform,
    historyRepositoryOld: HistoryRepositoryOld,
    historyRepositoryNew: HistoryRepositoryNew,
    logger: Logger
) : AbstractHistoricalHandler<DivideLotResult>(transform, historyRepositoryOld, historyRepositoryNew, logger) {

    override val action: CommandTypeV2
        get() = CommandTypeV2.DIVIDE_LOT

    override fun execute(descriptor: CommandDescriptor): Result<DivideLotResult, Fail> {
        val params = descriptor.body.asJsonNode
            .params<DivideLotRequest>()
            .flatMap { it.convert() }
            .flatMap { it.validateTextAttributes() }
            .onFailure { return it }
        return lotsService.divideLot(params = params)
    }

    private fun DivideLotParams.validateTextAttributes(): Result<DivideLotParams, DataErrors.Validation.EmptyString> {
        try {
            tender.lots.forEachIndexed { i, lot ->
                lot.id.checkForBlank("lots[$i].id")
                lot.internalId.checkForBlank("tender.lots[$i].internalId")
                lot.title.checkForBlank("tender.lots[$i].title")
                lot.description.checkForBlank("tender.lots[$i].description")
                lot.placeOfPerformance?.address?.let { address ->
                    address.streetAddress.checkForBlank("tender.lots[$i].placeOfPerformance.address.streetAddress")
                    address.postalCode.checkForBlank("tender.lots[$i].placeOfPerformance.address.postalCode")
                    address.addressDetails.country.id.checkForBlank("tender.lots[$i].placeOfPerformance.address.addressDetails.country.id")
                    address.addressDetails.region.id.checkForBlank("tender.lots[$i].placeOfPerformance.address.addressDetails.region.id")
                    address.addressDetails.locality.id.checkForBlank("tender.lots[$i].placeOfPerformance.address.addressDetails.locality.id")
                }
                lot.placeOfPerformance?.description.checkForBlank("tender.lots[$i].placeOfPerformance.description")
            }
            tender.items.forEachIndexed { i, item ->
                item.relatedLot.checkForBlank("tender.items[$i].relatedLot")
            }
        } catch (exception: EmptyStringException) {
            return DataErrors.Validation.EmptyString(exception.path).asFailure()
        }

        return this.asSuccess()
    }

    private fun String?.checkForBlank(name: String) = this.errorIfBlank { EmptyStringException(name) }
}
