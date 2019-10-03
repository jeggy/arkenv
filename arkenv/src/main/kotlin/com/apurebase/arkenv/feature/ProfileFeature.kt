package com.apurebase.arkenv.feature

import com.apurebase.arkenv.*

/**
 * Feature for loading profile-based configuration.
 * A list of active profiles can be configured via the *ARKENV_PROFILE* argument.
 * @param prefix the default prefix for any profile configuration files, can be set via *ARKENV_PROFILE_PREFIX*
 * @param locations defines the default list of locations in which to look for profile configuration files,
 * can be set via *ARKENV_PROFILE_LOCATION*
 * @param parsers additional providers for profile file parsing. By default supports the property format.
 */
class ProfileFeature(
    prefix: String = "application",
    locations: Collection<String> = listOf(),
    parsers: Collection<PropertyParser> = listOf()
) : ArkenvFeature, Arkenv("ProfileFeature", ArkenvBuilder(false)) {

    private val parsers: MutableList<PropertyParser> = mutableListOf(::PropertyFeature)

    init {
        this.parsers.addAll(parsers)
    }

    val active: List<String> by argument("--arkenv-profile") {
        defaultValue = ::emptyList
    }

    private val prefix: String by argument("--arkenv-profile-prefix") {
        defaultValue = { prefix }
    }

    private val location: Collection<String> by argument("--arkenv-profile-location") {
        defaultValue = { locations }
    }

    override fun onLoad(arkenv: Arkenv) {
        parse(arkenv.argList.toTypedArray())
        load(arkenv, null)
        active.forEach { load(arkenv, it) }
    }

    private fun load(arkenv: Arkenv, file: String?) = parsers
        .map { it(makeFileName(file), location) }
        .forEach { it.onLoad(arkenv) }

    private fun makeFileName(profile: String?) =
        if (profile != null) "$prefix-$profile"
        else prefix
}

typealias PropertyParser = (String, Collection<String>) -> PropertyFeature

val Arkenv.profiles get() = getFeature<ProfileFeature>()
