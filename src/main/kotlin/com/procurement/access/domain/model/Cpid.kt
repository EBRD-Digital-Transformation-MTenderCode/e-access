package com.procurement.access.domain.model

import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.Result.Companion.failure
import com.procurement.access.domain.util.Result.Companion.success

class Cpid private constructor(val value: String) {

    companion object {
        private val regex = "^([a-z]{4})-([a-z0-9]{6})-([A-Z]{2})-[0-9]{13}\$".toRegex()

        fun tryCreate(cpid: String): Result<Cpid, String> {
            return if (cpid.matches(regex = regex))
                success(Cpid(value = cpid))
            else
                failure(regex.pattern)
        }
    }
}
