package com.procurement.access.model.dto.items

import com.fasterxml.jackson.annotation.JsonCreator

data class ItemsRs @JsonCreator constructor(

        val tender: TenderItemsRs
)


data class TenderItemsRs @JsonCreator constructor(

        val classification: ClassificationTenderItemsRs
)

data class ClassificationTenderItemsRs @JsonCreator constructor(

        val id: String
)

fun getItemsRs(commonClass: String): ItemsRs = ItemsRs(TenderItemsRs(ClassificationTenderItemsRs(commonClass)))

