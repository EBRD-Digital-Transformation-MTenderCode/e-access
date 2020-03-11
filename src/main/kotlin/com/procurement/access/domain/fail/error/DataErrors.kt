package com.procurement.access.domain.fail.error

import com.procurement.access.domain.fail.Fail

sealed class DataErrors(numberError: String, override val description: String) : Fail.Error("DR-") {

    override val code: String = prefix + numberError

    class Parsing(description: String) : DataErrors(numberError = "0", description = description)

    sealed class Validation(numberError: String, val name: String, description: String) :
        DataErrors(numberError = numberError, description = description) {

        class MissingRequiredAttribute(name: String) :
            Validation(numberError = "1", description = "Missing required attribute.", name = name)

        class DataTypeMismatch(name: String, expectedType: String, actualType: String) :
            Validation(
                numberError = "2",
                description = "Data type mismatch. Expected data type: '$expectedType', actual data type: '$actualType'.",
                name = name
            )

        class UnknownValue(name: String, expectedValues: Collection<String>, actualValue: String) :
            Validation(
                numberError = "3",
                description = "Attribute value mismatch with one of enum expected values. Expected values: '${expectedValues.joinToString()}', actual value: '$actualValue'.",
                name = name
            )

        class DataFormatMismatch(name: String, expectedFormat: String, actualValue: String) :
            Validation(
                numberError = "4",
                description = "Data format mismatch. Expected data format: '$expectedFormat', actual value: '$actualValue'.",
                name = name
            )

        class EmptyArray(name: String) :
            Validation(numberError = "10", description = "Array is empty.", name = name)

    }
}