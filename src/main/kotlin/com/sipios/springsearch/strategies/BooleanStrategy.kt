package com.sipios.springsearch.strategies

import kotlin.reflect.KClass

class BooleanStrategy : ParsingStrategy {
    override fun parse(value: String?, fieldClass: KClass<out Any>): Any? {
        return value?.toBoolean()
    }
}
