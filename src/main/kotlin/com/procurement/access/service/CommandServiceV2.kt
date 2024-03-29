package com.procurement.access.service

import com.procurement.access.domain.fail.error.BadRequest
import com.procurement.access.infrastructure.api.v2.ApiResponseV2
import com.procurement.access.infrastructure.api.v2.CommandTypeV2
import com.procurement.access.infrastructure.api.v2.errorResponse
import com.procurement.access.infrastructure.handler.v2.AddClientsToPartiesInAPHandler
import com.procurement.access.infrastructure.handler.v2.CalculateAPValueHandler
import com.procurement.access.infrastructure.handler.v2.CheckAccessToTenderHandler
import com.procurement.access.infrastructure.handler.v2.CheckEqualityCurrenciesHandler
import com.procurement.access.infrastructure.handler.v2.CheckExistenceFAHandler
import com.procurement.access.infrastructure.handler.v2.CheckExistenceSignAuctionHandler
import com.procurement.access.infrastructure.handler.v2.CheckLotsStateHandler
import com.procurement.access.infrastructure.handler.v2.CheckPersonsStructureHandler
import com.procurement.access.infrastructure.handler.v2.CheckRelationHandler
import com.procurement.access.infrastructure.handler.v2.CheckTenderStateHandler
import com.procurement.access.infrastructure.handler.v2.CommandDescriptor
import com.procurement.access.infrastructure.handler.v2.CreateCriteriaForProcuringEntityHandler
import com.procurement.access.infrastructure.handler.v2.CreateRelationToContractProcessStageHandler
import com.procurement.access.infrastructure.handler.v2.CreateRelationToOtherProcessHandler
import com.procurement.access.infrastructure.handler.v2.CreateRfqHandler
import com.procurement.access.infrastructure.handler.v2.DefineTenderClassificationHandler
import com.procurement.access.infrastructure.handler.v2.DivideLotHandler
import com.procurement.access.infrastructure.handler.v2.FindAuctionsHandler
import com.procurement.access.infrastructure.handler.v2.FindCriteriaHandler
import com.procurement.access.infrastructure.handler.v2.FindLotIdsHandler
import com.procurement.access.infrastructure.handler.v2.GetBuyersOwnersHandler
import com.procurement.access.infrastructure.handler.v2.GetCurrencyHandler
import com.procurement.access.infrastructure.handler.v2.GetDataForContractHandler
import com.procurement.access.infrastructure.handler.v2.GetItemsByLotIdsHandler
import com.procurement.access.infrastructure.handler.v2.GetLotStateByIdsHandler
import com.procurement.access.infrastructure.handler.v2.GetLotsValueHandler
import com.procurement.access.infrastructure.handler.v2.GetMainProcurementCategoryHandler
import com.procurement.access.infrastructure.handler.v2.GetOrganizationsHandler
import com.procurement.access.infrastructure.handler.v2.GetQualificationCriteriaAndMethodHandler
import com.procurement.access.infrastructure.handler.v2.GetTenderStateHandler
import com.procurement.access.infrastructure.handler.v2.OutsourcingPNHandler
import com.procurement.access.infrastructure.handler.v2.PersonesProcessingHandler
import com.procurement.access.infrastructure.handler.v2.ResponderProcessingHandler
import com.procurement.access.infrastructure.handler.v2.SetStateForLotsHandler
import com.procurement.access.infrastructure.handler.v2.SetStateForTenderHandler
import com.procurement.access.infrastructure.handler.v2.ValidateClassificationHandler
import com.procurement.access.infrastructure.handler.v2.ValidateLotsDataForDivisionHandler
import com.procurement.access.infrastructure.handler.v2.ValidateRequirementResponsesHandler
import com.procurement.access.infrastructure.handler.v2.ValidateRfqDataHandler
import com.procurement.access.infrastructure.handler.v2.VerifyRequirementResponseHandler
import org.springframework.stereotype.Service

@Service
class CommandServiceV2(
    private val addClientsToPartiesInAPHandler: AddClientsToPartiesInAPHandler,
    private val calculateAPValueHandler: CalculateAPValueHandler,
    private val checkAccessToTenderHandler: CheckAccessToTenderHandler,
    private val checkEqualPNAndAPCurrencyHandler: CheckEqualityCurrenciesHandler,
    private val checkExistenceFAHandler: CheckExistenceFAHandler,
    private val checkExistenceSignAuctionHandler: CheckExistenceSignAuctionHandler,
    private val checkPersonsStructureHandler: CheckPersonsStructureHandler,
    private val checkLotsStateHandler: CheckLotsStateHandler,
    private val checkRelationHandler: CheckRelationHandler,
    private val checkTenderStateHandler: CheckTenderStateHandler,
    private val createCriteriaForProcuringEntityHandler: CreateCriteriaForProcuringEntityHandler,
    private val createRelationToContractProcessStageHandler: CreateRelationToContractProcessStageHandler,
    private val createRelationToOtherProcessHandler: CreateRelationToOtherProcessHandler,
    private val createRfqHandler: CreateRfqHandler,
    private val defineTenderClassificationHandler: DefineTenderClassificationHandler,
    private val divideLotHandler: DivideLotHandler,
    private val findAuctionsHandler: FindAuctionsHandler,
    private val findCriteriaHandler: FindCriteriaHandler,
    private val findLotIdsHandler: FindLotIdsHandler,
    private val getBuyersOwnersHandler: GetBuyersOwnersHandler,
    private val getCurrencyHandler: GetCurrencyHandler,
    private val getDataForContractHandler: GetDataForContractHandler,
    private val getItemsByLotIdsHandler: GetItemsByLotIdsHandler,
    private val getLotStateByIdsHandler: GetLotStateByIdsHandler,
    private val getLotsValueHandler: GetLotsValueHandler,
    private val getMainProcurementCategoryHandler: GetMainProcurementCategoryHandler,
    private val getOrganizationsHandler: GetOrganizationsHandler,
    private val getQualificationCriteriaAndMethodHandler: GetQualificationCriteriaAndMethodHandler,
    private val getTenderStateHandler: GetTenderStateHandler,
    private val outsourcingPNHandler: OutsourcingPNHandler,
    private val personesProcessingHandler: PersonesProcessingHandler,
    private val responderProcessingHandler: ResponderProcessingHandler,
    private val setStateForLotsHandler: SetStateForLotsHandler,
    private val setStateForTenderHandler: SetStateForTenderHandler,
    private val validateClassificationHandler: ValidateClassificationHandler,
    private val validateLotsDataForDivisionHandler: ValidateLotsDataForDivisionHandler,
    private val validateRequirementResponsesHandler: ValidateRequirementResponsesHandler,
    private val validateRfqDataHandler: ValidateRfqDataHandler,
    private val verifyRequirementResponseHandler: VerifyRequirementResponseHandler
) {

        fun execute(descriptor: CommandDescriptor): ApiResponseV2 =
            when (descriptor.action) {
                    CommandTypeV2.ADD_CLIENTS_TO_PARTIES_IN_AP -> addClientsToPartiesInAPHandler.handle(descriptor)
                    CommandTypeV2.CALCULATE_AP_VALUE -> calculateAPValueHandler.handle(descriptor)
                    CommandTypeV2.CHECK_ACCESS_TO_TENDER -> checkAccessToTenderHandler.handle(descriptor)
                    CommandTypeV2.CHECK_EQUALITY_CURRENCIES -> checkEqualPNAndAPCurrencyHandler.handle(descriptor)
                    CommandTypeV2.CHECK_EXISTENCE_FA -> checkExistenceFAHandler.handle(descriptor)
                    CommandTypeV2.CHECK_EXISTENCE_SIGN_AUCTION -> checkExistenceSignAuctionHandler.handle(descriptor)
                    CommandTypeV2.CHECK_LOTS_STATE -> checkLotsStateHandler.handle(descriptor)
                    CommandTypeV2.CHECK_PERSONES_STRUCTURE -> checkPersonsStructureHandler.handle(descriptor)
                    CommandTypeV2.CHECK_RELATION -> checkRelationHandler.handle(descriptor)
                    CommandTypeV2.CHECK_TENDER_STATE -> checkTenderStateHandler.handle(descriptor)
                    CommandTypeV2.CREATE_CRITERIA_FOR_PROCURING_ENTITY -> createCriteriaForProcuringEntityHandler.handle(descriptor)
                    CommandTypeV2.CREATE_RELATION_TO_CONTRACT_PROCESS_STAGE -> createRelationToContractProcessStageHandler.handle(descriptor)
                    CommandTypeV2.CREATE_RELATION_TO_OTHER_PROCESS -> createRelationToOtherProcessHandler.handle(descriptor)
                    CommandTypeV2.CREATE_RFQ -> createRfqHandler.handle(descriptor)
                    CommandTypeV2.DEFINE_TENDER_CLASSIFICATION -> defineTenderClassificationHandler.handle(descriptor)
                    CommandTypeV2.DIVIDE_LOT -> divideLotHandler.handle(descriptor)
                    CommandTypeV2.FIND_AUCTIONS -> findAuctionsHandler.handle(descriptor)
                    CommandTypeV2.FIND_CRITERIA -> findCriteriaHandler.handle(descriptor)
                    CommandTypeV2.FIND_LOT_IDS -> findLotIdsHandler.handle(descriptor)
                    CommandTypeV2.GET_BUYERS_OWNERS -> getBuyersOwnersHandler.handle(descriptor)
                    CommandTypeV2.GET_CURRENCY -> getCurrencyHandler.handle(descriptor)
                    CommandTypeV2.GET_DATA_FOR_CONTRACT -> getDataForContractHandler.handle(descriptor)
                    CommandTypeV2.GET_ITEMS_BY_LOT_IDS -> getItemsByLotIdsHandler.handle(descriptor)
                    CommandTypeV2.GET_LOTS_VALUE -> getLotsValueHandler.handle(descriptor)
                    CommandTypeV2.GET_LOT_STATE_BY_IDS -> getLotStateByIdsHandler.handle(descriptor)
                    CommandTypeV2.GET_MAIN_PROCUREMENT_CATEGORY -> getMainProcurementCategoryHandler.handle(descriptor)
                    CommandTypeV2.GET_ORGANIZATIONS -> getOrganizationsHandler.handle(descriptor)
                    CommandTypeV2.GET_QUALIFICATION_CRITERIA_AND_METHOD -> getQualificationCriteriaAndMethodHandler.handle(descriptor)
                    CommandTypeV2.GET_TENDER_STATE -> getTenderStateHandler.handle(descriptor)
                    CommandTypeV2.OUTSOURCING_PN -> outsourcingPNHandler.handle(descriptor)
                    CommandTypeV2.PERSONES_PROCESSING -> personesProcessingHandler.handle(descriptor)
                    CommandTypeV2.RESPONDER_PROCESSING -> responderProcessingHandler.handle(descriptor)
                    CommandTypeV2.SET_STATE_FOR_LOTS -> setStateForLotsHandler.handle(descriptor)
                    CommandTypeV2.SET_STATE_FOR_TENDER -> setStateForTenderHandler.handle(descriptor)
                    CommandTypeV2.VALIDATE_CLASSIFICATION -> validateClassificationHandler.handle(descriptor)
                    CommandTypeV2.VALIDATE_LOTS_DATA_FOR_DIVISION -> validateLotsDataForDivisionHandler.handle(descriptor)
                    CommandTypeV2.VALIDATE_REQUIREMENT_RESPONSES -> validateRequirementResponsesHandler.handle(descriptor)
                    CommandTypeV2.VALIDATE_RFQ_DATA -> validateRfqDataHandler.handle(descriptor)
                    CommandTypeV2.VERIFY_REQUIREMENT_RESPONSE -> verifyRequirementResponseHandler.handle(descriptor)

                    else -> {
                            val errorDescription = "Unknown action '${descriptor.action.key}'."
                            errorResponse(
                                fail = BadRequest(description = errorDescription, exception = RuntimeException(errorDescription)),
                                id = descriptor.id,
                                version = descriptor.version
                            )
                    }
            }
}
