package com.procurement.access.domain.model.owner

import com.procurement.access.domain.util.Result

typealias Owner = String

fun String.tryCreateOwner(): Result<Owner, String> =  Result.success(this)
