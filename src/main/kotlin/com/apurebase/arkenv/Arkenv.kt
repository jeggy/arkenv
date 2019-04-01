package com.apurebase.arkenv

import com.apurebase.arkenv.feature.CliFeature
import com.apurebase.arkenv.feature.EnvironmentVariableFeature

/**
 * The base class that provides the argument parsing capabilities.
 * Extend this to define your own arguments.
 * @param programName
 * @param configuration
 */
abstract class Arkenv(
    val programName: String = "Arkenv",
    configuration: (ArkenvBuilder.() -> Unit)? = null
) {

    internal val builder = ArkenvBuilder()
    internal val argList = mutableListOf<String>()
    internal val keyValue = mutableMapOf<String, String>()
    internal val delegates = mutableListOf<ArgumentDelegate<*>>()
    val help: Boolean by ArkenvDelegateLoader(listOf("-h", "--help"), false, { isHelp = true }, Boolean::class, this)

    init {
        builder.install(CliFeature())
        builder.install(EnvironmentVariableFeature())
        configuration?.invoke(builder)
    }

    /**
     * Parses the [args] and resets all previously parsed state.
     */
    fun parseArguments(args: Array<String>) {
        argList.addAll(args)
        onParse(args)
        builder.features.forEach { it.onLoad(this) }
        parse()
        argList.clear()
        keyValue.clear()
    }

    open fun onParse(args: Array<String>) {}

    open fun onParseArgument(name: String, argument: Argument<*>, value: Any?) {}

    override fun toString(): String = StringBuilder().apply {
        val indent = "    "
        val doubleIndent = indent + indent
        append("$programName: \n")
        delegates.forEach { delegate ->
            append(indent)
                .append(delegate.argument.names)
                .append(doubleIndent)
                .append(delegate.argument.description)
                .appendln()
                .append(doubleIndent)
                .append(delegate.property.name)
                .append(doubleIndent)
                .append(delegate.getValue(isParse = false))
                .appendln()
        }
    }.toString()

    private fun parse() {
        delegates
            .sortedBy { it.argument.isMainArg }
            .forEach {
                builder.features.forEach { feature ->
                    feature.configure(it.argument)
                }
                it.reset()
                val value = it.getValue(isParse = true)
                onParseArgument(it.property.name, it.argument, value)
            }
        parseBooleanMerge()
    }

    private fun parseBooleanMerge() =
        checkRemaining(delegates, argList).forEach { (arg, delegates) ->
            argList.remove("-$arg")
            delegates.forEach { it.setTrue() }
        }

    private fun ArgumentDelegate<*>.getValue(isParse: Boolean): Any? =
        getValue(this, property).also { value ->
            if (isParse && index != null) removeArgumentFromList(index!!, value)
        }

    private fun ArgumentDelegate<*>.removeArgumentFromList(index: Int, value: Any?) {
        removeValueArgument(index, isBoolean, value, isDefault)
        removeNameArgument(index, argument.isMainArg)
    }

    private fun removeNameArgument(index: Int, isMainArg: Boolean) {
        if (index > -1 && !isMainArg) argList.removeAt(index)
    }

    private fun removeValueArgument(
        index: Int, isBoolean: Boolean, value: Any?, default: Boolean
    ) {
        if (!isBoolean && !default && value != null) argList.removeAt(index + 1)
    }
}
