package com.procurement.access.infrastructure.configuration.properties

import com.procurement.access.infrastructure.api.ApiVersion
import com.procurement.access.infrastructure.io.orThrow
import java.util.*

object GlobalProperties {

    val service = Service()

    object App {
        val apiVersion = ApiVersion(major = 1, minor = 0, patch = 0)
    }

    class Service(
        val id: String = "3",
        val name: String = "e-access",
        val version: String = getGitProperties()
    )

    private fun getGitProperties(): String {
        val gitProps: Properties = try {
            GlobalProperties::class.java.getResourceAsStream("/git.properties")
                .use { stream ->
                    Properties().apply { load(stream) }
                }
        } catch (expected: Exception) {
            throw IllegalStateException(expected)
        }
        return gitProps.orThrow("git.commit.id.abbrev")
    }
}
