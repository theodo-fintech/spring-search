package com.sipios.springsearch.strategies

import com.sipios.springsearch.SearchOperation
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Predicate
import kotlin.reflect.KClass

class CollectionStrategy : ParsingStrategy {

    override fun buildPredicate(
        builder: CriteriaBuilder,
        path: Path<*>,
        fieldName: String,
        ops: SearchOperation?,
        value: Any?
    ): Predicate? {
        if (ops == SearchOperation.IS) {
            return builder.isEmpty(path[fieldName])
        }
        if (ops == SearchOperation.IS_NOT) {
            return builder.isNotEmpty(path[fieldName])
        }
        return super.buildPredicate(builder, path, fieldName, ops, value)
    }

    override fun parse(value: String?, fieldClass: KClass<out Any>): Any? {
        return value
    }
}
