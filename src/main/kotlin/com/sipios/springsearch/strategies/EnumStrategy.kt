package com.sipios.springsearch.strategies

import kotlin.reflect.KClass

class EnumStrategy : ParsingStrategy {
    override fun parse(value: String?, fieldClass: KClass<out Any>): Any? {
        return toValue(fieldClass, value)
    }

    private fun toValue(fieldClass: KClass<out Any>, value: Any?): Any? =
        Class.forName(fieldClass.qualifiedName).getMethod("valueOf", String::class.java).invoke(null, value)
}
