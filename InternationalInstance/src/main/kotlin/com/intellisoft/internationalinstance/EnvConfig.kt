package com.intellisoft.internationalinstance

import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class EnvConfig(private val environment: Environment) {
    fun getValue(): DbApplicationValues {
        val username = environment.getProperty("dhis.username")
        val password = environment.getProperty("dhis.password")
        val internationalUrl = environment.getProperty("dhis.international")
        val program = environment.getProperty("dhis.program")
        val template = environment.getProperty("dhis.template")

        return DbApplicationValues(internationalUrl, username, password, program, template)
    }
}