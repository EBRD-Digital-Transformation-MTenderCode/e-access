package com.procurement.access.model.dto.items

import com.fasterxml.jackson.annotation.JsonCreator
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class ItemsRequestDto @JsonCreator constructor(

        @field:NotEmpty @field:NotNull
        val items: HashSet<ItemRequestDto>
)


data class ItemRequestDto @JsonCreator constructor(

        @field:Valid @field:NotNull
        val classification: ClassificationRequestDto
)

data class ClassificationRequestDto @JsonCreator constructor(

        @field:NotNull
        val id: String
)
