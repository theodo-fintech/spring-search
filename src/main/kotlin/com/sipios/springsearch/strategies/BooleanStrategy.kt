package com.sipios.springsearch.strategies

import kotlin.reflect.KClass

class BooleanStrategy : ParsingStrategy {
    override fun parse(value: Any?, fieldClass: KClass<out Any>): Any? {
        if (value is String) return value.toBoolean()
        if (value is List<*>) return value.map { it.toString().toBoolean() }
        return value
    }
}
