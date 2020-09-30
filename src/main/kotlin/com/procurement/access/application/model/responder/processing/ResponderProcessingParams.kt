package com.procurement.access.application.model.responder.processing

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
import com.procurement.access.domain.model.persone.PersonId
import com.procurement.access.domain.util.None
import com.procurement.access.domain.util.Option
import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.Result.Companion.failure
import com.procurement.access.domain.util.Some
import java.time.LocalDateTime

class ResponderProcessing {

    class Params private constructor(
        val cpid: Cpid,
        val ocid: Ocid,
        val date: LocalDateTime,
        val responder: Responder
    ) {
        companion object {
            fun tryCreate(
                cpid: String,
                ocid: String,
                date: String,
                responder: Responder
            ): Result<Params, DataErrors> {

                val cpidResult = parseCpid(value = cpid)
                    .doOnError { error -> return failure(error) }
                    .get

                val ocidResult = parseOcid(value = ocid)
                    .doOnError { error -> return failure(error) }
                    .get

                val dateParsed = parseStartDate(date)
                    .doOnError { error -> return failure(error) }
                    .get

                return Result.success(
                    Params(
                        cpid = cpidResult,
                        ocid = ocidResult,
                        date = dateParsed,
                        responder = responder
                    )
                )
            }
        }

        class Responder private constructor(
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
                ): Result<Responder, DataErrors> {

                    return Result.success(
                        Responder(
                            id = PersonId.tryCreate(id)
                                .orForwardFail { return it },
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
                    fun tryCreate(scheme: String, id: String, uri: String?): Result<Identifier, DataErrors> {
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
                                BusinessFunctionType.AUTHORITY       -> false
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
                                    None    -> emptyList()
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
                                .doOnError { error -> return failure(error) }
                                .get

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