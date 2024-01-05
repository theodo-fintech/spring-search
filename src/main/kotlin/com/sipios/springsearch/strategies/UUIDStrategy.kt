package com.sipios.springsearch.strategies

import java.util.UUID
import kotlin.reflect.KClass

class UUIDStrategy : ParsingStrategy {
    override fun parse(value: Any?, fieldClass: KClass<out Any>): Any? {
        if (value is String) return UUID.fromString(value)
        if (value is List<*>) return value.map { UUID.fromString(it.toString()) }
        return value
    }
}
