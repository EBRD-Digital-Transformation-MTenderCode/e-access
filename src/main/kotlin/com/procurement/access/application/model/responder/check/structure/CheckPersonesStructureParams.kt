package com.procurement.access.application.model.responder.check.structure

import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.model.parseOcid
import com.procurement.access.application.model.parseStartDate
import com.procurement.access.domain.EnumElementProvider.Companion.keysAsStrings
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.document.DocumentId
import com.procurement.access.domain.model.enums.BusinessFunctionDocumentType
import com.procurement.access.domain.model.enums.BusinessFunctionType
import com.procurement.access.domain.model.enums.LocationOfPersonsType
import com.procurement.access.domain.model.persone.PersonId
import com.procurement.access.lib.functional.None
import com.procurement.access.lib.functional.Option
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.Result.Companion.failure
import com.procurement.access.lib.functional.Some
import java.time.LocalDateTime

class CheckPersonesStructure {

    class Params private constructor(
        val cpid: Cpid,
        val ocid: Ocid,
        val persones: List<Person>,
        val locationOfPersones: LocationOfPersonsType
    ) {
        companion object {
            private val allowedLocationOfPersonesTypes = LocationOfPersonsType.allowedElements
                .filter {
                    when (it) {
                        LocationOfPersonsType.AWARD,
                        LocationOfPersonsType.PROCURING_ENTITY -> true
                    }
                }.toSet()

            fun tryCreate(
                cpid: String,
                ocid: String,
                persones: List<Person>,
                locationOfPersones: String
            ): Result<Params, DataErrors> {

                val parsedCpid = parseCpid(value = cpid)
                    .onFailure { return it }

                val parsedOcid = parseOcid(value = ocid)
                    .onFailure { return it }

                val parsedLocationOfPersones = locationOfPersones
                    .let {
                        LocationOfPersonsType.orNull(it)
                            ?.takeIf { it in allowedLocationOfPersonesTypes }
                            ?: return failure(
                                DataErrors.Validation.UnknownValue(
                                    name = "locationOfPersones",
                                    expectedValues = allowedLocationOfPersonesTypes.keysAsStrings(),
                                    actualValue = it
                                )
                            )

                    }

                return Result.success(
                    Params(
                        cpid = parsedCpid,
                        ocid = parsedOcid,
                        persones = persones,
                        locationOfPersones = parsedLocationOfPersones
                    )
                )
            }
        }

        class Person private constructor(
            val id: PersonId,
            val title: String,
            val name: String,
            val identifier: Identifier,
            val businessFunctions: List<BusinessFunction>
        ) {

            companion object {
                fun tryCreate(
                    id: String,
                    title: String,
                    name: String,
                    identifier: Identifier,
                    businessFunctions: List<BusinessFunction>
                ): Result<Person, DataErrors> {

                    return Result.success(
                        Person(
                            id = PersonId.tryCreate(id)
                                .onFailure { return it },
                            title = title,
                            name = name,
                            identifier = identifier,
                            businessFunctions = businessFunctions
                        )
                    )
                }
            }

            class Identifier private constructor(
                val scheme: String,
                val id: String,
                val uri: String?
            ) {

                companion object {
                    fun tryCreate(
                        scheme: String,
                        id: String,
                        uri: String?
                    ): Result<Identifier, DataErrors> {

                        return Result.success(
                            Identifier(scheme = scheme, id = id, uri = uri)
                        )
                    }
                }
            }

            class BusinessFunction private constructor(
                val id: String,
                val type: BusinessFunctionType,
                val jobTitle: String,
                val period: Period,
                val documents: List<Document>
            ) {

                companion object {
                    private val allowedBusinessFunctionTypes = BusinessFunctionType.allowedElements
                        .filter {
                            when (it) {
                                BusinessFunctionType.CHAIRMAN,
                                BusinessFunctionType.PROCURMENT_OFFICER,
                                BusinessFunctionType.CONTACT_POINT,
                                BusinessFunctionType.TECHNICAL_EVALUATOR,
                                BusinessFunctionType.TECHNICAL_OPENER,
                                BusinessFunctionType.PRICE_OPENER,
                                BusinessFunctionType.PRICE_EVALUATOR -> true
                                BusinessFunctionType.AUTHORITY -> false
                            }
                        }.toSet()

                    fun tryCreate(
                        id: String,
                        type: String,
                        jobTitle: String,
                        period: Period,
                        documents: Option<List<Document>>
                    ): Result<BusinessFunction, DataErrors> {

                        val parsedType = type
                            .let {
                                BusinessFunctionType.orNull(it)
                                    ?.takeIf { it in allowedBusinessFunctionTypes }
                                    ?: return failure(
                                        DataErrors.Validation.UnknownValue(
                                            name = "businessFunction.type",
                                            expectedValues = allowedBusinessFunctionTypes.keysAsStrings(),
                                            actualValue = it
                                        )
                                    )
                            }

                        return Result.success(
                            BusinessFunction(
                                id = id,
                                type = parsedType,
                                jobTitle = jobTitle,
                                period = period,
                                documents = when (documents) {
                                    is Some -> documents.get
                                    None -> emptyList()
                                }
                            )
                        )
                    }
                }

                class Period private constructor(
                    val startDate: LocalDateTime
                ) {

                    companion object {
                        fun tryCreate(
                            startDate: String
                        ): Result<Period, DataErrors> {

                            val startDateParsed = parseStartDate(startDate)
                                .onFailure { return it }

                            return Result.success(
                                Period(startDate = startDateParsed)
                            )
                        }
                    }
                }

                class Document private constructor(
                    val id: DocumentId,
                    val documentType: BusinessFunctionDocumentType,
                    val title: String,
                    val description: String?
                ) {

                    companion object {
                        private val allowedBusinessFunctionDocumentTypes = BusinessFunctionDocumentType.allowedElements
                            .filter {
                                when (it) {
                                    BusinessFunctionDocumentType.REGULATORY_DOCUMENT -> true
                                }
                            }.toSet()

                        fun tryCreate(
                            id: String,
                            documentType: String,
                            title: String,
                            description: String?
                        ): Result<Document, DataErrors> {

                            val createdDocumentType = documentType
                                .let {
                                    BusinessFunctionDocumentType.orNull(it)
                                        ?.takeIf { it in allowedBusinessFunctionDocumentTypes }
                                        ?: return failure(
                                            DataErrors.Validation.UnknownValue(
                                                name = "documentType",
                                                expectedValues = allowedBusinessFunctionDocumentTypes.keysAsStrings(),
                                                actualValue = it
                                            )
                                        )
                                }

                            return Result.success(
                                Document(
                                    id = id,
                                    documentType = createdDocumentType,
                                    title = title,
                                    description = description
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
