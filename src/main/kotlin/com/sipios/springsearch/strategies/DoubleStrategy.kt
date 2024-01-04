package com.sipios.springsearch.strategies

import com.sipios.springsearch.SearchOperation
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Predicate
import kotlin.reflect.KClass

class DoubleStrategy : ParsingStrategy {
    override fun buildPredicate(
        builder: CriteriaBuilder,
        path: Path<*>,
        fieldName: String,
        ops: SearchOperation?,
        value: Any?
    ): Predicate? {
        return when (ops) {
            SearchOperation.GREATER_THAN -> builder.greaterThan(path[fieldName], value as Double)
            SearchOperation.LESS_THAN -> builder.lessThan(path[fieldName], value as Double)
            SearchOperation.GREATER_THAN_EQUALS -> builder.greaterThanOrEqualTo(path[fieldName], value as Double)
            SearchOperation.LESS_THAN_EQUALS -> builder.lessThanOrEqualTo(path[fieldName], value as Double)
            else -> super.buildPredicate(builder, path, fieldName, ops, value)
        }
    }

    override fun parse(value: String?, fieldClass: KClass<out Any>): Any? {
        return value?.toDouble()
    }
}
