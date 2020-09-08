package com.procurement.access.domain

import com.procurement.access.exception.EnumElementProviderException

object EnumElementProviderParser {

    fun <T> checkAndParseEnum(
        value: String,
        allowedValues: Set<T>,
        target: EnumElementProvider<T>
    ): T where T : Enum<T>,
               T : EnumElementProvider.Key = target.orNull(value)
        ?.takeIf { it in allowedValues }
        ?: throw EnumElementProviderException(
            enumType = target::class.java.canonicalName,
            value = value,
            values = allowedValues.joinToString { it.key })
}