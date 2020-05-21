package com.procurement.access.domain.model.persone

import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.Result.Companion.failure
import com.procurement.access.domain.util.Result.Companion.success

class PersonId(private val value: String) {

    override fun toString(): String = value

    companion object {

        fun tryCreate(text: String): Result<PersonId, DataErrors> =
            if (text.isBlank())
                failure(DataErrors.Validation.EmptyString(name = "id"))
            else
                success(PersonId(text))
    }
}
