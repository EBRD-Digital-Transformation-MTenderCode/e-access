package com.procurement.access.domain.model

import kotlin.jvm.internal.Intrinsics

interface Entity<ID> {
    val id: ID

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}

abstract class EntityBase<ID> : Entity<ID> {
    override fun equals(other: Any?): Boolean {
        return if (this !== other) {
            other is EntityBase<*> && Intrinsics.areEqual(this.id, other.id)
        } else
            true
    }

    override fun hashCode(): Int = id.hashCode()
}

fun <ID> Iterable<Entity<ID>>.isUniqueIds(): Boolean {
    val unique = mutableSetOf<ID>()
    forEach { item ->
        if (!unique.add(item.id)) return false
    }
    return true
}

fun <ID> Iterable<Entity<ID>>.isNotUniqueIds(): Boolean = !this.isUniqueIds()

inline fun <ID, E : Entity<ID>, T : Exception> Iterable<E>.isNotUniqueIds(block: (E) -> T) {
    val unique = mutableSetOf<ID>()
    forEach { item ->
        if (!unique.add(item.id)) throw block(item)
    }
}

inline fun <ID, E : Entity<ID>, T : Exception> Iterable<E>.isNotUniqueIds(ids: Set<ID>, block: (E) -> T): Set<ID> {
    val unique = mutableSetOf<ID>().apply { addAll(ids) }
    forEach { item ->
        if (!unique.add(item.id)) throw block(item)
    }
    return unique
}

fun <ID, E : Entity<ID>> Iterable<E>.uniqueIds(): Set<ID>? {
    val unique = mutableSetOf<ID>()
    forEach { item ->
        if (!unique.add(item.id)) return null
    }
    return unique
}

inline fun <ID, S : Entity<ID>, D : Entity<ID>> Iterable<D>.update(sources: Iterable<S>, block: (D, S) -> D): List<D> =
    this.update(sources.associateBy { it.id }, block)

inline fun <ID, S : Entity<ID>, D : Entity<ID>> Iterable<D>.update(sources: Map<ID, S>, block: (D, S) -> D): List<D> =
    this.map { destination ->
        sources[destination.id]
            ?.let { source -> block(destination, source) }
            ?: destination
    }
