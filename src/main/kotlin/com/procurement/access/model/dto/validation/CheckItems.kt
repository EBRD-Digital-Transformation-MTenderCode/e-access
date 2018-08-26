package com.procurement.access.model.dto.validation

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import java.util.*
import javax.validation.constraints.NotEmpty

data class CheckItemsRq @JsonCreator constructor(

        @field: NotEmpty
        val items: HashSet<ItemCheck>
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CheckItemsRs @JsonCreator constructor(

        val mdmValidation: Boolean,

        val itemsAdd: Boolean? = null,

        val tender: TenderCheck? = null
)

data class TenderCheck @JsonCreator constructor(

        var classification: ClassificationCheck
)

data class ItemCheck @JsonCreator constructor(

        val id: String,

        val classification: ClassificationCheck
)

data class ClassificationCheck @JsonCreator constructor(

        val id: String
)
