package com.procurement.access.config

import com.procurement.access.infrastructure.web.dto.ApiVersion
import java.util.*

object GlobalProperties {

    val service = Service()

    object App {
        val apiVersion = ApiVersion(major = 1, minor = 0, patch = 0)
    }

    class Service(
        val id: String = "9",
        val name: String = "e-access",
        val version: String = getGitProperties()
    )

    private fun getGitProperties(): String {
        val prop = Properties()
        val loader = Thread.currentThread().contextClassLoader
        val stream = loader.getResourceAsStream("git.properties")
        if (stream != null) {
            prop.load(stream)
            return prop.getProperty("git.commit.id.abbrev")
        } else {
            throw RuntimeException("Unable to find git.commit.id.abbrev")
        }
    }
}
