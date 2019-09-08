package com.sipios.springsearch.strategies

import com.fasterxml.jackson.databind.util.StdDateFormat
import com.sipios.springsearch.SearchOperation
import java.text.DateFormat
import java.util.Date
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Path
import javax.persistence.criteria.Predicate
import kotlin.reflect.KClass

class DateStrategy : ParsingStrategy {
    private val sdf: DateFormat = StdDateFormat()

    override fun buildPredicate(
        builder: CriteriaBuilder,
        path: Path<*>,
        fieldName: String,
        ops: SearchOperation?,
        value: Any?
    ): Predicate? {
        return when (ops) {
            SearchOperation.GREATER_THAN -> builder.greaterThan(path.get<Date>(fieldName), value as Date)
            SearchOperation.LESS_THAN -> builder.lessThan(path.get<Date>(fieldName), value as Date)
            else -> super.buildPredicate(builder, path, fieldName, ops, value)
        }
    }

    override fun parse(value: String?, fieldClass: KClass<out Any>): Any? {
        return sdf.parse(value)
    }
}
