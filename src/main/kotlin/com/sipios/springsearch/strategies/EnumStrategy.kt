package com.sipios.springsearch.strategies

import java.util.Locale
import kotlin.reflect.KClass

class EnumStrategy : ParsingStrategy {
    override fun parse(value: String?, fieldClass: KClass<out Any>): Any? {
        return try {
            Class
                .forName(fieldClass.qualifiedName)
                .getMethod("valueOf", String::class.java)
                .invoke(null, value?.uppercase(Locale.getDefault()))
        } catch (e: Exception) {
            value?.uppercase(Locale.getDefault())
        }
    }
}
