package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator

data class SourceParty @JsonCreator constructor(

        var id: String,

        val name: String
)
