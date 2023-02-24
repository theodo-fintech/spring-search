package com.sipios.springsearch.strategies

import com.fasterxml.jackson.databind.util.StdDateFormat
import com.sipios.springsearch.SearchOperation
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Predicate
import java.text.DateFormat
import java.util.Date
import kotlin.reflect.KClass

class DateStrategy : ParsingStrategy {
    private val standardDateFormat: DateFormat = StdDateFormat()

    override fun buildPredicate(
        builder: CriteriaBuilder,
        path: Path<*>,
        fieldName: String,
        ops: SearchOperation?,
        value: Any?
    ): Predicate? {
        return when (ops) {
            SearchOperation.GREATER_THAN -> builder.greaterThan(path.get(fieldName), value as Date)
            SearchOperation.LESS_THAN -> builder.lessThan(path.get(fieldName), value as Date)
            else -> super.buildPredicate(builder, path, fieldName, ops, value)
        }
    }

    override fun parse(value: String?, fieldClass: KClass<out Any>): Any? {
        return standardDateFormat.parse(value)
    }
}
