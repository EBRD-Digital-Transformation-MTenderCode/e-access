package com.procurement.access.domain.util.extension

import com.procurement.access.domain.util.Option
import com.procurement.access.domain.util.Result

fun <T> T?.toList(): List<T> = if (this != null) listOf(this) else emptyList()

inline fun <T, V> Collection<T>.isUnique(selector: (T) -> V): Boolean {
    val unique = HashSet<V>()
    forEach { item ->
        if (!unique.add(selector(item))) return false
    }
    return true
}

inline fun <T, V> Collection<T>.toSetBy(selector: (T) -> V): Set<V> {
    val collections = LinkedHashSet<V>()
    forEach {
        collections.add(selector(it))
    }
    return collections
}

fun <T, R, E> List<T>.mapResult(block: (T) -> Result<R, E>): Result<List<R>, E> {
    val r = mutableListOf<R>()
    for (element in this) {
        when (val result = block(element)) {
            is Result.Success -> r.add(result.get)
            is Result.Failure -> return result
        }
    }
    return Result.success(r)
}

fun <T, R, E> List<T>?.mapOptionalResult(block: (T) -> Result<R, E>): Result<Option<List<R>>, E> {
    if (this == null)
        return Result.success(Option.none())
    val r = mutableListOf<R>()
    for (element in this) {
        when (val result = block(element)) {
            is Result.Success -> r.add(result.get)
            is Result.Failure -> return result
        }
    }
    return Result.success(Option.pure(r))
}

fun <T> getUnknownElements(received: Iterable<T>, known: Iterable<T>) =
    getNewElements(received = received, known = known)

fun <T> getNewElements(received: Iterable<T>, known: Iterable<T>): Set<T> =
    received.asSet().subtract(known.asSet())

fun <T> getMissingElements(received: Iterable<T>, known: Iterable<T>): Set<T> =
    known.asSet().subtract(received.asSet())

fun <T> getElementsForUpdate(received: Set<T>, known: Set<T>) = known.intersect(received)

inline fun <T, V> Collection<T>.getDuplicate(selector: (T) -> V): T? {
    val unique = HashSet<V>()
    this.forEach { item ->
        if (!unique.add(selector(item)))
            return item
    }
    return null
}

private fun <T> Iterable<T>.asSet(): Set<T> = when (this) {
    is Set -> this
    else -> this.toSet()
}