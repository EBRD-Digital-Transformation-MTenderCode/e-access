package com.procurement.access.infrastructure.dto.converter

import com.procurement.access.application.service.CheckResponsesData
import com.procurement.access.infrastructure.dto.CheckResponsesRequest

fun CheckResponsesRequest.convert(): CheckResponsesData {
    return CheckResponsesData(
        items = items
            .map { item ->
                CheckResponsesData.Item(
                    id = item.id
                )
            },
        bid = bid.let { bid ->
            CheckResponsesData.Bid(
                requirementResponses = bid.requirementResponses
                    ?.map { requirementResponse ->
                        CheckResponsesData.Bid.RequirementResponse(
                            id = requirementResponse.id,
                            description = requirementResponse.description,
                            title = requirementResponse.title,
                            value = requirementResponse.value,
                            period = requirementResponse.period
                                ?.let { period ->
                                    CheckResponsesData.Bid.RequirementResponse.Period(
                                        startDate = period.startDate,
                                        endDate = period.endDate
                                    )
                                },
                            requirement = CheckResponsesData.Bid.RequirementResponse.Requirement(
                                id = requirementResponse.requirement.id
                            )
                        )
                    }
                    .orEmpty(),
                relatedLots = bid.relatedLots
            )
        }
    )
}
