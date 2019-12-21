package com.procurement.access.application.model

sealed class Mode{
    abstract val pattern: Regex
}
data class TestMode(override val pattern: Regex): Mode()
data class MainMode(override val pattern: Regex): Mode()
