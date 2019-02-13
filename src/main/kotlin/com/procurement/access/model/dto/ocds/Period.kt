package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.procurement.access.model.dto.databinding.JsonDateTimeDeserializer
import com.procurement.access.model.dto.databinding.JsonDateTimeSerializer
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Period @JsonCreator constructor(
    @JsonDeserialize(using = JsonDateTimeDeserializer::class)
    @JsonSerialize(using = JsonDateTimeSerializer::class)
    val startDate: LocalDateTime,

    @JsonDeserialize(using = JsonDateTimeDeserializer::class)
    @JsonSerialize(using = JsonDateTimeSerializer::class)
    val endDate: LocalDateTime?
)
