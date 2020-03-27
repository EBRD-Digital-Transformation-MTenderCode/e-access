package com.procurement.access.domain

import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.Result.Companion.failure
import com.procurement.access.domain.util.Result.Companion.success
import com.procurement.access.exception.EnumElementProviderException

abstract class EnumElementProvider<T>(val info: EnumInfo<T>) where T : Enum<T>,
                                                                   T : EnumElementProvider.Key {

    @Target(AnnotationTarget.PROPERTY)
    annotation class DeprecatedElement

    @Target(AnnotationTarget.PROPERTY)
    annotation class ExcludedElement

    interface Key {
        val key: String
    }

    class EnumInfo<T>(
        val target: Class<T>,
        val values: Array<T>
    )

    companion object {
        inline fun <reified T : Enum<T>> info() = EnumInfo(target = T::class.java, values = enumValues())
    }

    private val elements: Map<String, T> = info.values.associateBy { it.key.toUpperCase() }

    val allowedValues: List<String> = info.values
        .asSequence()
        .filter { element -> element.isNotExcluded() }
        .map { element -> element.key + if (element.isDeprecated()) " (Deprecated)" else "" }
        .toList()

    fun orNull(key: String): T? = elements[key.toUpperCase()]

    fun orThrow(key: String): T = orNull(key)
        ?: throw EnumElementProviderException(
            enumType = info.target.canonicalName,
            value = key,
            values = info.values.joinToString { it.key }
        )

    fun tryOf(key: String): Result<T, String> {
        val element = orNull(key)
        return if (element != null)
            success(element)
        else {
            val enumType = info.target.canonicalName
            val allowedValues = info.values.joinToString { it.key }
            failure("Unknown value '$key' for enum type '$enumType'. Allowed values are '$allowedValues'.")
        }
    }

    operator fun contains(key: String): Boolean = orNull(key) != null

    private fun <E : Enum<E>> Enum<E>.isNotExcluded(): Boolean = this.findAnnotation<ExcludedElement, E>() == null
    private fun <E : Enum<E>> Enum<E>.isDeprecated(): Boolean = this.findAnnotation<DeprecatedElement, E>() != null
    private inline fun <reified A : Annotation, E : Enum<E>> Enum<E>.findAnnotation(): A? = this.javaClass
        .getDeclaredField(this.name)
        .getAnnotation(A::class.java)
}
