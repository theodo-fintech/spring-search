package com.sipios.springsearch.strategies

import com.sipios.springsearch.SearchOperation
import java.util.UUID
import kotlin.reflect.KClass

class UUIDStrategy : ParsingStrategy {
    override fun parse(value: String?, fieldClass: KClass<out Any>): Any? {
        if (value == SearchOperation.NULL) return value
        return UUID.fromString(value)
    }
}
