package com.procurement.access.domain.fail.error

import com.procurement.access.domain.fail.Fail

sealed class DataErrors(numberError: String, override val description: String, val attributeName: String) :
    Fail.Error("DR-") {

    override val code: String = prefix + numberError

    class MissingRequiredAttribute(attributeName: String) :
        DataErrors(numberError = "1", description = "Missing required attribute.", attributeName = attributeName)

    class DataTypeMismatch(attributeName: String) :
        DataErrors(numberError = "2", description = "Data type mismatch.", attributeName = attributeName)

    class UnknownValue(attributeName: String) :
        DataErrors(
            numberError = "3",
            description = "Attribute value mismatch with one of enum expected values",
            attributeName = attributeName
        )

    class DataFormatMismatch(attributeName: String) :
        DataErrors(numberError = "4", description = "Data format mismatch.", attributeName = attributeName)

    class DataMismatchToPattern(attributeName: String) :
        DataErrors(numberError = "5", description = "Data mismatch to pattern.", attributeName = attributeName)

    class UniquenessDataMismatch(attributeName: String) :
        DataErrors(numberError = "6", description = "Uniqueness data mismatch.", attributeName = attributeName)

    class InvalidNumberOfElementsInArray(attributeName: String) :
        DataErrors(
            numberError = "7",
            description = "Invalid number of objects in the array.",
            attributeName = attributeName
        )

    class InvalidStringLength(attributeName: String) :
        DataErrors(numberError = "8", description = "Invalid number of chars in string.", attributeName = attributeName)

    class EmptyObject(attributeName: String) :
        DataErrors(numberError = "9", description = "Object is empty.", attributeName = attributeName)

    class EmptyArray(attributeName: String) :
        DataErrors(numberError = "10", description = "Array is empty.", attributeName = attributeName)

    class EmptyString(attributeName: String) :
        DataErrors(numberError = "11", description = "String is empty.", attributeName = attributeName)

    class UnexpectedAttribute(attributeName: String) :
        DataErrors(numberError = "12", description = "Unexpected attribute.", attributeName = attributeName)

    class Detail(val name: String)
}