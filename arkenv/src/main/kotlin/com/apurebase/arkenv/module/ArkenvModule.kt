package com.apurebase.arkenv.module

import com.apurebase.arkenv.parse.ArkenvParser
import com.apurebase.arkenv.ModuleInitializationException
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

interface ArkenvModule<T : Any> : ReadOnlyProperty<Any, T> {

    val kClass: KClass<T>

    var module: T?

    fun initialize(arkenvParser: ArkenvParser<*>) {
        if (module == null) {
            module = arkenvParser.createInstance(kClass)
        }
        arkenvParser.parse(module!!)
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        if (module != null) {
            return module!!
        }
        else throw ModuleInitializationException(kClass.java.name)
    }
}
