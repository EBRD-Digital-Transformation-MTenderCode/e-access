package com.procurement.access.infrastructure.io

import java.util.*

fun Properties.orThrow(name: String): String = this[name]
    ?.toString()
    ?: throw IllegalStateException("Property '$name' is not found.")