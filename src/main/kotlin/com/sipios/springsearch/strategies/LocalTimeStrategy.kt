package com.sipios.springsearch.strategies

import com.sipios.springsearch.SearchOperation
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Predicate
import java.time.LocalTime
import kotlin.reflect.KClass

class LocalTimeStrategy : ParsingStrategy {
    override fun buildPredicate(
        builder: CriteriaBuilder,
        path: Path<*>,
        fieldName: String,
        ops: SearchOperation?,
        value: Any?
    ): Predicate? {
        return when (ops) {
            SearchOperation.GREATER_THAN -> builder.greaterThan(path[fieldName], value as LocalTime)
            SearchOperation.LESS_THAN -> builder.lessThan(path[fieldName], value as LocalTime)
            SearchOperation.GREATER_THAN_EQUALS -> builder.greaterThanOrEqualTo(path[fieldName], value as LocalTime)
            SearchOperation.LESS_THAN_EQUALS -> builder.lessThanOrEqualTo(path[fieldName], value as LocalTime)
            else -> super.buildPredicate(builder, path, fieldName, ops, value)
        }
    }

    override fun parse(value: String?, fieldClass: KClass<out Any>): Any? {
        if (value == SearchOperation.NULL) return value
        return LocalTime.parse(value)
    }
}
