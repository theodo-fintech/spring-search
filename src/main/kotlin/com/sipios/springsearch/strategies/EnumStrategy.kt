package com.sipios.springsearch.strategies

import com.sipios.springsearch.SearchOperation
import kotlin.reflect.KClass

class EnumStrategy : ParsingStrategy {
    override fun parse(value: String?, fieldClass: KClass<out Any>): Any? {
        if (value == SearchOperation.NULL) return value
        return Class.forName(fieldClass.qualifiedName).getMethod("valueOf", String::class.java).invoke(null, value)
    }
}
