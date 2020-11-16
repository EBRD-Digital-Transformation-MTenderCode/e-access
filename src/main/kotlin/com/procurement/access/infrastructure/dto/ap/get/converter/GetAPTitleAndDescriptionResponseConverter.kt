package com.procurement.access.infrastructure.dto.ap.get.converter

import com.procurement.access.application.service.ap.get.GetAPTitleAndDescriptionResult
import com.procurement.access.infrastructure.dto.ap.get.GetAPTitleAndDescriptionResponse

fun GetAPTitleAndDescriptionResult.convert(): GetAPTitleAndDescriptionResponse =
    GetAPTitleAndDescriptionResponse(
        title = this.title,
        description = this.description
    )