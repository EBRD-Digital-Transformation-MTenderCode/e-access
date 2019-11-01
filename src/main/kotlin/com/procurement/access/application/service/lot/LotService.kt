package com.procurement.access.application.service.lot

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.lot.LotId
import com.procurement.access.domain.model.money.Money
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.lib.orThrow
import com.procurement.access.model.dto.ocds.Lot
import com.procurement.access.model.dto.ocds.TenderProcess
import com.procurement.access.utils.toObject
import org.springframework.stereotype.Service

interface LotService {
    fun getLot(context: GetLotContext): GettedLot

    fun getLotsForAuction(context: LotsForAuctionContext, data: LotsForAuctionData): LotsForAuction
}

@Service
class LotServiceImpl(
    private val tenderProcessDao: TenderProcessDao
) : LotService {

    override fun getLot(context: GetLotContext): GettedLot {
        val entity = tenderProcessDao.getByCpIdAndStage(context.cpid, context.stage)
            ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)

        return toObject(CNEntity::class.java, entity.jsonData)
            .tender
            .lots
            .find { lot ->
                LotId.fromString(lot.id) == context.lotId
            }
            ?.let { lot ->
                GettedLot(
                    id = lot.id,
                    internalId = lot.internalId,
                    title = lot.title,
                    description = lot.description,
                    status = lot.status,
                    statusDetails = lot.statusDetails,
                    value = Money(amount = lot.value.amount, currency = lot.value.currency),
                    options = lot.options.map { option ->
                        GettedLot.Option(
                            hasOptions = option.hasOptions
                        )
                    },
                    variants = lot.variants.map { variant ->
                        GettedLot.Variant(
                            hasVariants = variant.hasVariants
                        )
                    },
                    renewals = lot.renewals.map { renewal ->
                        GettedLot.Renewal(
                            hasRenewals = renewal.hasRenewals
                        )
                    },
                    recurrentProcurement = lot.recurrentProcurement.map { recurrentProcurement ->
                        GettedLot.RecurrentProcurement(
                            isRecurrent = recurrentProcurement.isRecurrent
                        )
                    },
                    contractPeriod = lot.contractPeriod.let { contractPeriod ->
                        GettedLot.ContractPeriod(
                            startDate = contractPeriod.startDate,
                            endDate = contractPeriod.endDate
                        )
                    },
                    placeOfPerformance = lot.placeOfPerformance.let { placeOfPerformance ->
                        GettedLot.PlaceOfPerformance(
                            description = placeOfPerformance.description,
                            address = placeOfPerformance.address.let { address ->
                                GettedLot.PlaceOfPerformance.Address(
                                    streetAddress = address.streetAddress,
                                    postalCode = address.postalCode,
                                    addressDetails = address.addressDetails.let { addressDetails ->
                                        GettedLot.PlaceOfPerformance.Address.AddressDetails(
                                            country = addressDetails.country.let { country ->
                                                GettedLot.PlaceOfPerformance.Address.AddressDetails.Country(
                                                    scheme = country.scheme,
                                                    id = country.id,
                                                    description = country.description,
                                                    uri = country.uri
                                                )
                                            },
                                            region = addressDetails.region.let { region ->
                                                GettedLot.PlaceOfPerformance.Address.AddressDetails.Region(
                                                    scheme = region.scheme,
                                                    id = region.id,
                                                    description = region.description,
                                                    uri = region.uri
                                                )
                                            },
                                            locality = addressDetails.locality.let { locality ->
                                                GettedLot.PlaceOfPerformance.Address.AddressDetails.Locality(
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
                    }
                )
            }
            .orThrow {
                ErrorException(
                    error = ErrorType.LOT_NOT_FOUND,
                    message = "In tender by cpid '${context.cpid}' and stage '${context.stage}' the lot by id '${context.lotId}' not found."
                )
            }
    }

    override fun getLotsForAuction(context: LotsForAuctionContext, data: LotsForAuctionData): LotsForAuction {
        return tenderProcessDao.getByCpIdAndStage(context.cpid, context.prevStage)
            ?.let { entity ->
                val process = toObject(TenderProcess::class.java, entity.jsonData)
                getLotFromTender(lots = process.tender.lots)
                    .takeIf {
                        it.lots.isNotEmpty()
                    }
                    ?: getLotFromRequest(lots = data.lots)
            }
            ?: getLotFromRequest(lots = data.lots)
    }

    private fun getLotFromRequest(lots: List<LotsForAuctionData.Lot>): LotsForAuction = LotsForAuction(
        lots = lots.map { lot ->
            LotsForAuction.Lot(
                id = lot.id,
                value = lot.value.let { value ->
                    LotsForAuction.Lot.Value(
                        amount = value.amount,
                        currency = value.currency
                    )
                }
            )
        }
    )

    private fun getLotFromTender(lots: List<Lot>): LotsForAuction = LotsForAuction(
        lots = lots.asSequence()
            .filter { it.status == LotStatus.PLANNING }
            .map { lot ->
                LotsForAuction.Lot(
                    id = lot.id,
                    value = lot.value.let { value ->
                        LotsForAuction.Lot.Value(
                            amount = value.amount,
                            currency = value.currency
                        )
                    }
                )
            }
            .toList()
    )
}