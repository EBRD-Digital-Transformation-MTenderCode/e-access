package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Document(

        @JsonProperty("id") @NotNull
        val id: String,

        @JsonProperty("documentType") @NotNull
        val documentType: DocumentType,

        @JsonProperty("title")
        val title: String?,

        @JsonProperty("description")
        val description: String?,

        @JsonProperty("language") @NotNull
        val language: String,

        @JsonProperty("relatedLots")
        var relatedLots: HashSet<String>?
)
