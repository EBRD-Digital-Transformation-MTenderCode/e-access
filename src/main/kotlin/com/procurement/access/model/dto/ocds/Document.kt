package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
//@JsonPropertyOrder("id", "documentType", "title", "description", "language", "relatedLots")
data class Document(

        @JsonProperty("id")
        val id: String,

        @JsonProperty("documentType")
        val documentType: DocumentType,

        @JsonProperty("title")
        val title: String?,

        @JsonProperty("description")
        val description: String?,

        @JsonProperty("language")
        val language: String,

        @JsonProperty("relatedLots")
        var relatedLots: HashSet<String>?
)
