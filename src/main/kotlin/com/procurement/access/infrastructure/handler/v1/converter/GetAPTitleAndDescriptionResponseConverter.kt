package com.procurement.access.infrastructure.handler.v1.converter

import com.procurement.access.application.service.ap.get.GetAPTitleAndDescriptionResult
import com.procurement.access.infrastructure.handler.v1.model.response.GetAPTitleAndDescriptionResponse

fun GetAPTitleAndDescriptionResult.convert(): GetAPTitleAndDescriptionResponse =
    GetAPTitleAndDescriptionResponse(
        title = this.title,
        description = this.description
    )