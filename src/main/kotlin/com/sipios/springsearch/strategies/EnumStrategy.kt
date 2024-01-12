package com.sipios.springsearch.strategies

import kotlin.reflect.KClass

class EnumStrategy : ParsingStrategy {
    override fun parse(value: String?, fieldClass: KClass<out Any>): Any? {
        if (value == null) return null
        return Class.forName(fieldClass.qualifiedName).getMethod("valueOf", String::class.java).invoke(null, value)
    }
}
