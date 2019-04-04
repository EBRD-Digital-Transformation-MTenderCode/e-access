package com.procurement.access.domain.model

typealias CPVCode = String

typealias CPVCodePattern = String

private const val COUNT_SYMBOLS_PATTERN_GROUPS = 3
private const val CPV_CODE_LENGTH = 8

val CPVCode.patternOfGroups: CPVCodePattern
    get() = this.patternBySymbols(COUNT_SYMBOLS_PATTERN_GROUPS)

fun CPVCode.patternBySymbols(countSymbols: Int): CPVCodePattern {
    return this.take(countSymbols)
}

fun CPVCode.startsWithPattern(pattern: CPVCodePattern): Boolean {
    return this.startsWith(pattern)
}

fun Collection<CPVCode>.startsWithPattern(pattern: CPVCodePattern): Boolean {
    return this.all { code ->
        code.startsWithPattern(pattern)
    }
}

fun CPVCodePattern.toCPVCode(): CPVCode {
    return this.padEnd(CPV_CODE_LENGTH, '0')
}
