package com.procurement.access.infrastructure

import com.procurement.access.json.testMappingOnFail
import com.procurement.access.json.testingBindingAndMapping

abstract class AbstractDTOTestBase<T : Any>(private val target: Class<T>) {
    fun testBindingAndMapping(pathToJsonFile: String) {
        testingBindingAndMapping(pathToJsonFile, target)
    }

    fun testMappingOnFail(pathToJsonFile: String) {
        testMappingOnFail(pathToJsonFile, target)
    }
}
