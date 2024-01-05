package com.sipios.springsearch.strategies

import kotlin.reflect.KClass

class EnumStrategy : ParsingStrategy {
    override fun parse(value: Any?, fieldClass: KClass<out Any>): Any? {
        if (value is String) return toValue(fieldClass, value)
        if (value is List<*>) return value.map { toValue(fieldClass, it.toString()) }
        return value
    }

    private fun toValue(fieldClass: KClass<out Any>, value: Any?): Any? =
        Class.forName(fieldClass.qualifiedName).getMethod("valueOf", String::class.java).invoke(null, value)
}
