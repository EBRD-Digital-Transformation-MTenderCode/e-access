package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonProperty

data class RecurrentProcurement(

        @JsonProperty("isRecurrent")
        @get:JsonProperty("isRecurrent")
        val isRecurrent: Boolean
)