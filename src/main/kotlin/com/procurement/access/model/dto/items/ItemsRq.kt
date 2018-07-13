package com.procurement.access.model.dto.items

import com.fasterxml.jackson.annotation.JsonCreator
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class ItemsRq @JsonCreator constructor(

        @field:NotEmpty @field:NotNull
        val items: HashSet<ItemItemsRq>
)


data class ItemItemsRq @JsonCreator constructor(

        @field:Valid @field:NotNull
        val classification: ClassificationItemItemsRq
)

data class ClassificationItemItemsRq @JsonCreator constructor(

        @field:NotNull
        val id: String
)
