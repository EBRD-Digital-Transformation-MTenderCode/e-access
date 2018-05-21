package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonProperty

data class JointProcurement(

        @JsonProperty("isJointProcurement")
        @get:JsonProperty("isJointProcurement")
        val isJointProcurement: Boolean
)