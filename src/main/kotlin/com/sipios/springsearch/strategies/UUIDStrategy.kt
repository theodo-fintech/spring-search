package com.sipios.springsearch.strategies

import java.util.UUID
import kotlin.reflect.KClass

class UUIDStrategy : ParsingStrategy {
    override fun parse(value: String?, fieldClass: KClass<out Any>): Any? {
        return UUID.fromString(value)
    }
}
