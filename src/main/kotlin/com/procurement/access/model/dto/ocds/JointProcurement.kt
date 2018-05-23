package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.NotNull

data class JointProcurement(

        @NotNull
        @JsonProperty("isJointProcurement")
        @get:JsonProperty("isJointProcurement")
        val isJointProcurement: Boolean
)