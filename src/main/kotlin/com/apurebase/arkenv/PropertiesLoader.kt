package com.apurebase.arkenv

import java.util.*

fun loadProperties(arkenv: Arkenv) {
    parseProperties(arkenv.propertiesFile).let(arkenv.dotEnv::putAll)
}

private fun parseProperties(propertiesFile: String?): Map<String, String> = when {
    propertiesFile != null -> Properties()
        .apply {
            Arkenv::class.java.classLoader
                .getResourceAsStream(propertiesFile)
                .use(::load)
        }
        .map { (key, value) -> key.toString().toUpperCase() to value.toString() }
        .toMap()
    else -> mapOf()
}
