package com.sipios.springsearch.strategies

import com.sipios.springsearch.SearchOperation
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Predicate
import java.time.LocalDate
import kotlin.reflect.KClass

class LocalDateStrategy : ParsingStrategy {
    override fun buildPredicate(
        builder: CriteriaBuilder,
        path: Path<*>,
        fieldName: String,
        ops: SearchOperation?,
        value: Any?
    ): Predicate? {
        return when (ops) {
            SearchOperation.GREATER_THAN -> builder.greaterThan(path[fieldName], value as LocalDate)
            SearchOperation.LESS_THAN -> builder.lessThan(path[fieldName], value as LocalDate)
            SearchOperation.GREATER_THAN_EQUALS -> builder.greaterThanOrEqualTo(path[fieldName], value as LocalDate)
            SearchOperation.LESS_THAN_EQUALS -> builder.lessThanOrEqualTo(path[fieldName], value as LocalDate)
            else -> super.buildPredicate(builder, path, fieldName, ops, value)
        }
    }

    override fun parse(value: Any?, fieldClass: KClass<out Any>): Any? {
        if (value is String) return LocalDate.parse(value)
        if (value is List<*>) return value.map { LocalDate.parse(it.toString()) }
        return value
    }
}
