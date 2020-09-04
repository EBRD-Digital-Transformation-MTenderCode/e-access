package com.procurement.access.domain.model.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.domain.EnumElementProvider

enum class RelatedProcessType(@JsonValue override val key: String) : EnumElementProvider.Key {

    FRAMEWORK("framework"),
    PARENT("parent"),
    PLANNING("planning"),
    X_CONTRACTING("x_contracting"),
    X_EVALUATION("x_evaluation"),
    X_EXECUTION("x_execution"),
    X_EXPENDITURE_ITEM("x_expenditureItem"),
    X_FUNDING_SOURCE("x_fundingSource"),
    X_NEGOTIATION("x_negotiation"),
    X_PLANNED("x_planned"),
    X_PRESELECTION("x_preselection"),
    X_PRE_QUALIFICATION("x_prequalification");

    override fun toString(): String = key

    companion object : EnumElementProvider<RelatedProcessType>(info = info()) {

        @JvmStatic
        @JsonCreator
        fun creator(name: String) = orThrow(name)
    }
}
