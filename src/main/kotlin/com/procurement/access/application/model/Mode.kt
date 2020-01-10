package com.procurement.access.application.model

sealed class Mode{
    abstract val prefix: String
    abstract val pattern: Regex
}
data class TestMode(override val prefix: String, override val pattern: Regex): Mode()
data class MainMode(override val prefix: String, override val pattern: Regex): Mode()
